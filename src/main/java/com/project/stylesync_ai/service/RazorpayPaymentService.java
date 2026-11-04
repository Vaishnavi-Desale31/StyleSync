package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.RazorpayOrderResponse;
import com.project.stylesync_ai.dto.RazorpayVerifyRequest;
import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.entity.OrderItem;
import com.project.stylesync_ai.repository.AddressRepository;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RazorpayPaymentService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    private final AddressRepository addressRepository;

    public RazorpayPaymentService(
            CartRepository cartRepository,
            OrderRepository orderRepository,
            AddressRepository addressRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
    }

    private void checkCredentials() {
        if (keyId == null || keyId.isBlank()
                || keySecret == null || keySecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay credentials are not configured"
            );
        }
    }

    @Transactional
    public RazorpayOrderResponse createPaymentOrder(
            Long cartId, String email, Long addressId) {

        checkCredentials();

        Cart cart = cartRepository.findByIdAndUser_Email(cartId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot pay for an empty cart");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setCartId(cartId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PAYMENT_PENDING");
        order.setPaymentMethod("RAZORPAY");
        order.setPaymentStatus("PENDING");
        order.applyDeliveryAddress(
                DeliveryAddresses.require(
                        addressRepository, addressId, email));

        for (CartItem cartItem : cart.getItems()) {
            if (!cartItem.getProduct().isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product " + cartItem.getProduct().getName()
                                + " is no longer available. "
                                + "Please remove it from your cart.");
            }

            BigDecimal price = cartItem.getProduct().getPrice();

            total = total.add(
                    price.multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())));

            OrderItem item = new OrderItem();
            item.setProductId(cartItem.getProduct().getId());
            item.setProductName(cartItem.getProduct().getName());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setOrder(order);

            orderItems.add(item);
        }

        total = total.setScale(2, RoundingMode.HALF_UP);

        long amountPaise = total.movePointRight(2).longValueExact();

        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        try {
            JSONObject request = new JSONObject();
            request.put("amount", amountPaise);
            request.put("currency", "INR");
            request.put("receipt", "stylesync_" + savedOrder.getId());
            request.put("notes", new JSONObject()
                    .put("local_order_id", savedOrder.getId()));

            JSONObject razorpayOrder = createRemoteOrder(request);

            String razorpayOrderId = razorpayOrder.getString("id");

            savedOrder.setRazorpayOrderId(razorpayOrderId);
            orderRepository.save(savedOrder);

            return new RazorpayOrderResponse(
                    savedOrder.getId(),
                    razorpayOrderId,
                    amountPaise,
                    "INR",
                    keyId
            );

        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to create Razorpay order"
            );
        }
    }

    // Separate method so tests can simulate Razorpay API failures.
    JSONObject createRemoteOrder(JSONObject request) throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        return client.orders.create(request).toJson();
    }

    @Transactional
    public Order verifyPayment(
            RazorpayVerifyRequest request, String email) {

        if (request == null
                || request.getOrderId() == null
                || request.getRazorpayOrderId() == null
                || request.getRazorpayOrderId().isBlank()
                || request.getRazorpayPaymentId() == null
                || request.getRazorpayPaymentId().isBlank()
                || request.getRazorpaySignature() == null
                || request.getRazorpaySignature().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment verification fields are required");
        }

        checkCredentials();

        Order order = orderRepository.findByIdAndUser_Email(
                        request.getOrderId(), email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (!"RAZORPAY".equals(order.getPaymentMethod())
                || order.getRazorpayOrderId() == null
                || !order.getRazorpayOrderId().equals(
                request.getRazorpayOrderId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Razorpay order does not match");
        }

        boolean signatureValid;
        try {
            signatureValid = Utils.verifySignature(
                    order.getRazorpayOrderId() + "|"
                            + request.getRazorpayPaymentId(),
                    request.getRazorpaySignature(),
                    keySecret);
        } catch (Exception exception) {
            signatureValid = false;
        }

        if (!signatureValid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment signature");
        }

        if ("PAID".equals(order.getPaymentStatus())) {
            if (request.getRazorpayPaymentId().equals(
                    order.getRazorpayPaymentId())) {
                return order;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order has already been paid");
        }

        // Only an order that is still awaiting payment can be paid.
        if (!"PAYMENT_PENDING".equals(order.getStatus())
                || !"PENDING".equals(order.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is not awaiting payment");
        }

        try {
            JSONObject payment = fetchPayment(
                    request.getRazorpayPaymentId());

            if (!order.getRazorpayOrderId().equals(
                    payment.getString("order_id"))
                    || !"captured".equalsIgnoreCase(
                    payment.getString("status"))) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Payment has not been captured for this order");
            }

            long expectedPaise = order.getTotalAmount()
                    .setScale(2, RoundingMode.HALF_UP)
                    .movePointRight(2)
                    .longValueExact();

            if (payment.getLong("amount") != expectedPaise
                    || !"INR".equalsIgnoreCase(
                    payment.getString("currency"))) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Payment amount or currency does not match");
            }

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify payment with Razorpay");
        }

        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setPaymentStatus("PAID");
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.save(order);

        // Clear the cart only if it still matches the paid order.
        Cart cart = cartRepository.findByIdAndUser_Email(
                        order.getCartId(), email)
                .orElse(null);

        if (cart != null && cartMatchesOrder(cart, order)) {
            cart.clearItems();
            cartRepository.save(cart);
        }

        return savedOrder;
    }

    private boolean cartMatchesOrder(Cart cart, Order order) {
        if (cart.getItems() == null || order.getItems() == null
                || cart.getItems().size() != order.getItems().size()) {
            return false;
        }

        for (CartItem cartItem : cart.getItems()) {
            boolean matched = order.getItems().stream().anyMatch(
                    orderItem ->
                            orderItem.getProductId().equals(
                                    cartItem.getProduct().getId())
                                    && orderItem.getQuantity().equals(
                                    cartItem.getQuantity()));

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    RazorpayClient createRazorpayClient() throws Exception {
        return new RazorpayClient(keyId, keySecret);
    }

    JSONObject fetchPayment(String paymentId) throws Exception {
        RazorpayClient client = createRazorpayClient();

        return client.payments.fetch(paymentId).toJson();
    }
}
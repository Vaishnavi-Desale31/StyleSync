package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.entity.OrderItem;
import com.project.stylesync_ai.repository.AddressRepository;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(
        name = "app.demo-payment.enabled",
        havingValue = "true")
public class DemoPaymentService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    private final AddressRepository addressRepository;

    public DemoPaymentService(
            CartRepository cartRepository,
            OrderRepository orderRepository,
            AddressRepository addressRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Order createDemoOrder(
            Long cartId, String email, Long addressId) {

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
        order.recordStatus(
                "PAYMENT_PENDING",
                "Demo payment initiated"
        );        order.setPaymentMethod("DEMO");
        order.setPaymentStatus("PENDING");
        order.applyDeliveryAddress(
                DeliveryAddresses.require(
                        addressRepository, addressId, email));

        for (CartItem cartItem : cart.getItems()) {

            if (!cartItem.getProduct().isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product is no longer available: "
                                + cartItem.getProduct().getName());
            }

            BigDecimal price = cartItem.getProduct().getPrice();

            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid product price: "
                                + cartItem.getProduct().getName()
                );
            }

            total = total.add(price.multiply(
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
        order.setTotalAmount(total);
        order.setItems(orderItems);

        return orderRepository.save(order);
    }

    @Transactional
    public Order retryDemoPayment(Long orderId, String email) {

        Order order = findDemoOrder(orderId, email);

        if (!"PAYMENT_FAILED".equals(order.getStatus())
                || !"FAILED".equals(order.getPaymentStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only failed demo orders can retry payment"
            );
        }

        order.setPaymentStatus("PENDING");

        order.recordStatus(
                "PAYMENT_PENDING",
                "Demo payment retry initiated"
        );

        return orderRepository.save(order);
    }

    @Transactional
    public Order simulateSuccess(Long orderId, String email) {

        Order order = findDemoOrder(orderId, email);

        if ("PAID".equals(order.getPaymentStatus())) {
            return order;
        }

        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending demo payments can be completed");
        }

        order.setPaymentStatus("PAID");

        order.recordStatus(
                "PLACED",
                "Demo payment completed successfully"
        );

        Order savedOrder = orderRepository.save(order);

        Cart cart = cartRepository.findByIdAndUser_Email(
                        order.getCartId(), email)
                .orElse(null);

        if (cart != null && cartMatchesOrder(cart, order)) {
            cart.clearItems();
            cartRepository.save(cart);
        }

        return savedOrder;
    }

    @Transactional
    public Order simulateFailure(Long orderId, String email) {

        Order order = findDemoOrder(orderId, email);

        if ("FAILED".equals(order.getPaymentStatus())) {
            return order;
        }

        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending demo payments can be failed");
        }

        order.setPaymentStatus("FAILED");

        order.recordStatus(
                "PAYMENT_FAILED",
                "Demo payment failed"
        );

        return orderRepository.save(order);
    }

    private Order findDemoOrder(Long orderId, String email) {

        Order order = orderRepository.findByIdAndUser_Email(
                        orderId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (!"DEMO".equals(order.getPaymentMethod())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This endpoint only supports demo payments");
        }

        return order;
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
}

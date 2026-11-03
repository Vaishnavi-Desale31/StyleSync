package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.entity.OrderItem;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MockPaymentService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Value("${payment.mock.enabled:false}")
    private boolean mockPaymentEnabled;

    public MockPaymentService(
            CartRepository cartRepository,
            OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    private void checkMockPaymentEnabled() {
        if (!mockPaymentEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Mock payment is disabled"
            );
        }
    }

    @Transactional
    public Order createMockPaymentOrder(Long cartId, String email) {

        checkMockPaymentEnabled();

        Cart cart = cartRepository.findByIdAndUser_Email(cartId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot pay for an empty cart"
            );
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setCartId(cartId);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod("MOCK");
        order.setPaymentStatus("PENDING");
        order.recordStatus(
                "PAYMENT_PENDING",
                "Waiting for mock payment"
        );

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            if (!cartItem.getProduct().isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Product " + cartItem.getProduct().getName()
                                + " is inactive. Please remove it from your cart."
                );
            }

            OrderItem item = new OrderItem();

            item.setProductId(cartItem.getProduct().getId());
            item.setProductName(cartItem.getProduct().getName());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setOrder(order);

            totalAmount = totalAmount.add(
                    item.getPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity())));

            orderItems.add(item);
        }

        order.setTotalAmount(
                totalAmount.setScale(2, RoundingMode.HALF_UP));
        order.setItems(orderItems);

        // The cart is deliberately NOT cleared here.
        return orderRepository.save(order);
    }

    @Transactional
    public Order completeMockPayment(
            Long orderId,
            String email,
            String outcome) {

        checkMockPaymentEnabled();

        if (outcome == null || outcome.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment outcome is required"
            );
        }

        String result = outcome.trim().toUpperCase();

        if (!result.equals("SUCCESS") && !result.equals("FAILURE")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Outcome must be SUCCESS or FAILURE"
            );
        }

        Order order = orderRepository.findByIdAndUser_Email(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        if (!"MOCK".equals(order.getPaymentMethod())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This is not a mock payment order"
            );
        }

        // Allow safe retries of the same completed result.
        if ("PAID".equals(order.getPaymentStatus())) {
            if (result.equals("SUCCESS")) {
                return order;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment has already succeeded"
            );
        }

        if ("FAILED".equals(order.getPaymentStatus())) {
            if (result.equals("FAILURE")) {
                return order;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment has already failed"
            );
        }

        if (!"PENDING".equals(order.getPaymentStatus())
                || !"PAYMENT_PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is not awaiting payment"
            );
        }

        for (OrderItem item : order.getItems()) {
            boolean active = cartRepository
                    .findByIdAndUser_Email(order.getCartId(), email)
                    .map(cart -> cart.getItems().stream()
                            .anyMatch(cartItem ->
                                    cartItem.getProduct().getId().equals(item.getProductId())
                                            && cartItem.getProduct().isActive()))
                    .orElse(false);

            if (!active) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A product in this order is inactive. Payment cannot be completed."
                );
            }
        }

        if (result.equals("FAILURE")) {
            order.setPaymentStatus("FAILED");
            order.recordStatus(
                    "PAYMENT_FAILED",
                    "Mock payment failed"
            );

            return orderRepository.save(order);
        }

        order.setPaymentStatus("PAID");
        order.recordStatus(
                "PLACED",
                "Mock payment successful"
        );

        // Clear the cart only if it still contains the ordered items.
        Cart cart = cartRepository.findByIdAndUser_Email(
                order.getCartId(),
                email
        ).orElse(null);

        if (cart != null && cartMatchesOrder(cart, order)) {
            cart.clearItems();
            cartRepository.save(cart);
        }

        return orderRepository.save(order);
    }

    private boolean cartMatchesOrder(Cart cart, Order order) {

        Map<Long, Integer> cartItems = new HashMap<>();
        Map<Long, Integer> orderedItems = new HashMap<>();

        for (CartItem item : cart.getItems()) {
            cartItems.merge(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        for (OrderItem item : order.getItems()) {
            orderedItems.merge(
                    item.getProductId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        return cartItems.equals(orderedItems);
    }
}
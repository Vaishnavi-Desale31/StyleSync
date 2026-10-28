
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.entity.OrderItem;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
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
public class DemoPaymentService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public DemoPaymentService(
            CartRepository cartRepository,
            OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createDemoOrder(Long cartId, String email) {

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
        order.setPaymentMethod("DEMO");
        order.setPaymentStatus("PENDING");

        for (CartItem cartItem : cart.getItems()) {
            BigDecimal price = BigDecimal.valueOf(
                    cartItem.getProduct().getPrice());

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
        order.setTotalAmount(total.doubleValue());
        order.setItems(orderItems);

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
        order.setStatus("PLACED");

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
        order.setStatus("PAYMENT_FAILED");

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

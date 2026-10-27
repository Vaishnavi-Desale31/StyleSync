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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private OrderRepository orderRepository;
    private CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository){
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public Order createOrderFromCart(Long cartId, String email) {
        return createOrderFromCartInternal(cartId, email, null, null);
    }

    @Transactional
    public Order createCodOrderFromCart(Long cartId, String email) {
        return createOrderFromCartInternal(
                cartId,
                email,
                "COD",
                "PENDING"
        );
    }

    private Order createOrderFromCartInternal(
            Long cartId,
            String email,
            String paymentMethod,
            String paymentStatus) {

        Cart cart = cartRepository.findByIdAndUser_Email(cartId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot create an order from an empty cart"
            );
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");

        if (paymentMethod != null) {
            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(paymentStatus);
        }

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();

            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setOrder(order);

            totalAmount += orderItem.getPrice()
                    * cartItem.getQuantity();

            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cart.clearItems();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, String email){
        return orderRepository.findByIdAndUser_Email(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders(String email) {
        return orderRepository.findAllByUser_Email(email);
    }
}

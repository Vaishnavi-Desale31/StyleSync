package com.project.stylesync_ai.controller;


import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/cod/{cartId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order createCodOrder(
            @PathVariable Long cartId,
            Authentication authentication) {
        return orderService.createCodOrderFromCart(
                cartId,
                authentication.getName());
    }

    @GetMapping("/{orderId}")
    public Order getOrderById(
            @PathVariable Long orderId,
            Authentication authentication){
        return orderService.getOrderById(
                orderId,
                authentication.getName());
    }

    @GetMapping
    public List<Order> getAllOrders(Authentication authentication) {
        return orderService.getAllOrders(authentication.getName());
    }
}

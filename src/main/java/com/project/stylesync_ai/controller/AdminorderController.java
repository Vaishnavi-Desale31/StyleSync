package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.UpdateOrderStatusRequest;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order management for administrators. Access is restricted to the ADMIN
 * role in SecurityConfig (/api/admin/**).
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrdersForAdmin();
    }

    @PatchMapping("/{orderId}/status")
    public Order updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(
                orderId, request.getStatus(), request.getNote());
    }
}
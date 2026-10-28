
package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.service.DemoPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/demo")
public class DemoPaymentController {

    private final DemoPaymentService demoPaymentService;

    public DemoPaymentController(
            DemoPaymentService demoPaymentService) {
        this.demoPaymentService = demoPaymentService;
    }

    @PostMapping("/orders/{cartId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order createDemoOrder(
            @PathVariable Long cartId,
            Authentication authentication) {

        return demoPaymentService.createDemoOrder(
                cartId, authentication.getName());
    }

    @PostMapping("/orders/{orderId}/success")
    public Order simulateSuccess(
            @PathVariable Long orderId,
            Authentication authentication) {

        return demoPaymentService.simulateSuccess(
                orderId, authentication.getName());
    }

    @PostMapping("/orders/{orderId}/failure")
    public Order simulateFailure(
            @PathVariable Long orderId,
            Authentication authentication) {

        return demoPaymentService.simulateFailure(
                orderId, authentication.getName());
    }
}

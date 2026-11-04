package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.CheckoutRequest;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.service.MockPaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/mock")
public class MockPaymentController {

    private final MockPaymentService mockPaymentService;

    public MockPaymentController(MockPaymentService mockPaymentService) {
        this.mockPaymentService = mockPaymentService;
    }

    // Create a pending mock payment order
    @PostMapping("/orders/{cartId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order createMockPaymentOrder(
            @PathVariable Long cartId,
            @RequestBody(required = false) CheckoutRequest request,
            Authentication authentication) {

        return mockPaymentService.createMockPaymentOrder(
                cartId,
                authentication.getName(),
                request == null ? null : request.getAddressId()
        );
    }

    // Simulate payment success or failure (development only)
    @PostMapping("/orders/{orderId}/complete")
    public Order completeMockPayment(
            @PathVariable Long orderId,
            @RequestParam String outcome,
            Authentication authentication) {

        return mockPaymentService.completeMockPayment(
                orderId,
                authentication.getName(),
                outcome
        );
    }
}
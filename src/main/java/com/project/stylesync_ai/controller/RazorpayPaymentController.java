package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.RazorpayOrderResponse;
import com.project.stylesync_ai.dto.RazorpayVerifyRequest;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.service.RazorpayPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayPaymentController {

    private final RazorpayPaymentService paymentService;

    public RazorpayPaymentController(
            RazorpayPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{cartId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RazorpayOrderResponse createPaymentOrder(
            @PathVariable Long cartId,
            Authentication authentication) {

        return paymentService.createPaymentOrder(
                cartId, authentication.getName());
    }

    @PostMapping("/verify")
    public Order verifyPayment(
            @RequestBody RazorpayVerifyRequest request,
            Authentication authentication) {

        return paymentService.verifyPayment(
                request, authentication.getName());
    }
}
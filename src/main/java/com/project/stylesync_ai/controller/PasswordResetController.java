








package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.ForgotPasswordRequest;
import com.project.stylesync_ai.dto.ResetPasswordRequest;
import com.project.stylesync_ai.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Public endpoints (covered by /api/auth/** in SecurityConfig). */
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());

        return Map.of("message",
                "If that email is registered, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(
                request.token(), request.newPassword());

        return Map.of("message", "Your password has been reset.");
    }
}
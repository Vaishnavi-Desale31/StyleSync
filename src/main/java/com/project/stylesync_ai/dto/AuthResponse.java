package com.project.stylesync_ai.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs
) {
}

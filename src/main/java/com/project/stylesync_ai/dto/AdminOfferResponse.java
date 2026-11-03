package com.project.stylesync_ai.dto;

import java.math.BigDecimal;

public record AdminOfferResponse(
        Long id,
        String platform,
        BigDecimal price,
        String productUrl
) {
}
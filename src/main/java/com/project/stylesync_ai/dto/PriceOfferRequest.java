package com.project.stylesync_ai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PriceOfferRequest(

        @NotBlank(message = "Platform name is required")
        @Size(max = 100, message = "Platform name must not exceed 100 characters")
        String platform,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal price,

        @NotBlank(message = "Product URL is required")
        @Size(max = 1000, message = "Product URL must not exceed 1000 characters")
        String productUrl
) {
}

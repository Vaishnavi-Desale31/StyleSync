















































package com.project.stylesync_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 50, message = "Label cannot exceed 50 characters")
        String label,

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        String phone,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 200, message = "Address line 1 cannot exceed 200 characters")
        String line1,

        @Size(max = 200, message = "Address line 2 cannot exceed 200 characters")
        String line2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State cannot exceed 100 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(max = 12, message = "Postal code cannot exceed 12 characters")
        String postalCode,

        @Size(max = 100, message = "Country cannot exceed 100 characters")
        String country,

        Boolean defaultAddress
) {
}
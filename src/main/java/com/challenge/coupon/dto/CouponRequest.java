package com.challenge.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponRequest(
        @NotBlank(message = "code is required")
        String code,
        @NotBlank(message = "description is required")
        String description,
        @NotNull(message = "discountValue is required")
        @DecimalMin(value = "0.5", message = "discountValue must be at least 0.5")
        BigDecimal discountValue,
        @NotNull(message = "expirationDate is required")
        LocalDate expirationDate,
        boolean published) {
}
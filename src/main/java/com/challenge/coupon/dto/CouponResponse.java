package com.challenge.coupon.dto;

import com.challenge.coupon.domain.Coupon;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record CouponResponse(UUID id, String code, String description, BigDecimal discountValue,
                             LocalDate expirationDate, boolean published) {
    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountValue(coupon.getDiscountValue())
                .expirationDate(coupon.getExpirationDate())
                .published(coupon.isPublished())
                .build();
    }
}
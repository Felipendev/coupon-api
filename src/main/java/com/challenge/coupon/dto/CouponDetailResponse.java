package com.challenge.coupon.dto;

import com.challenge.coupon.domain.Coupon;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
public class CouponDetailResponse {
    UUID id;
    String code;
    String description;
    BigDecimal discountValue;
    LocalDate expirationDate;
    boolean published;

    public CouponDetailResponse(Coupon coupon) {
        this.id = coupon.getId();
        this.code = coupon.getCode();
        this.description = coupon.getDescription();
        this.discountValue = coupon.getDiscountValue();
        this.expirationDate = coupon.getExpirationDate();
        this.published = coupon.isPublished();
    }
}
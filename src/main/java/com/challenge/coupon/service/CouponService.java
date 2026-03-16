package com.challenge.coupon.service;

import com.challenge.coupon.domain.Coupon;
import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;

import java.util.UUID;

public interface CouponService {
    CouponResponse create(CouponRequest request);
    Coupon couponDetail(UUID id);
}
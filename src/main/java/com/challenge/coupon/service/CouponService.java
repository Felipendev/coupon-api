package com.challenge.coupon.service;

import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;

public interface CouponService {
    CouponResponse create(CouponRequest request);
}
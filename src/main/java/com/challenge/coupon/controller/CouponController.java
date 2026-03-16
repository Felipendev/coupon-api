package com.challenge.coupon.controller;

import com.challenge.coupon.domain.Coupon;
import com.challenge.coupon.dto.CouponDetailResponse;
import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import com.challenge.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponAPI{

    private final CouponService service;

    @Override
    public CouponResponse create(CouponRequest request) {
        return service.create(request);
    }

    @Override
    public CouponDetailResponse couponDetail(UUID id) {
        Coupon coupon = service.couponDetail(id);
        return new CouponDetailResponse(coupon);
    }

    @Override
    public void delete(UUID id) {
    service.delete(id);
    }
}
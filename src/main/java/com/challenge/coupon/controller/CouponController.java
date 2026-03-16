package com.challenge.coupon.controller;

import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import com.challenge.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponAPI{

    private final CouponService service;

    @Override
    public CouponResponse create(CouponRequest request) {
        return service.create(request);
    }
}
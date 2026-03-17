package com.challenge.coupon.controller;

import com.challenge.coupon.domain.Coupon;
import com.challenge.coupon.dto.CouponDetailResponse;
import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import com.challenge.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponAPI {

    private final CouponService service;

    @Override
    public ResponseEntity<CouponResponse> create(CouponRequest request) {
        CouponResponse body = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Override
    public ResponseEntity<CouponDetailResponse> couponDetail(UUID id) {
        Coupon coupon = service.couponDetail(id);
        return ResponseEntity.ok(CouponDetailResponse.from(coupon));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
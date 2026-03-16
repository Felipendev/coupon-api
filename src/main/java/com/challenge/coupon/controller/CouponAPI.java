package com.challenge.coupon.controller;

import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import com.challenge.coupon.dto.CouponDetailResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping("/coupon")
public interface CouponAPI {

    @PostMapping
    ResponseEntity<CouponResponse> create(@RequestBody @Valid CouponRequest request);

    @GetMapping("/{id}")
    ResponseEntity<CouponDetailResponse> couponDetail(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
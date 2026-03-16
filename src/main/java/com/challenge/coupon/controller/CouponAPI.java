package com.challenge.coupon.controller;

import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/coupon")
public interface CouponAPI {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CouponResponse create(@RequestBody @Valid CouponRequest request);
}
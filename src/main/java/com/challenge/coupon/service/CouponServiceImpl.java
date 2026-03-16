package com.challenge.coupon.service;

import com.challenge.coupon.domain.Coupon;
import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import com.challenge.coupon.exception.BusinessException;
import com.challenge.coupon.repository.CouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository repository;

    @Override
    @Transactional
    public CouponResponse create(CouponRequest request) {
        try {
            Coupon coupon = Coupon.create(
                    request.code(),
                    request.description(),
                    request.discountValue(),
                    request.expirationDate(),
                    request.published()
            );

            Coupon savedCoupon = repository.save(coupon);

            return CouponResponse.from(savedCoupon);
        } catch (DataIntegrityViolationException exception) {
            log.error("[error] CouponServiceImpl.create duplicate code={}", request.code(), exception);
            throw new BusinessException("Coupon code already exists");
        }
    }

    @Override
    public Coupon couponDetail(UUID id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Coupon not found", HttpStatus.NOT_FOUND));
    }
}
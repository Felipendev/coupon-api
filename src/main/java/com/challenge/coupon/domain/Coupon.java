package com.challenge.coupon.domain;

import com.challenge.coupon.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "coupon")
@Getter
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE coupon SET deleted = true WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MIN_DISCOUNT_VALUE = new BigDecimal("0.5");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = CODE_LENGTH, updatable = false)
    private String code;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean deleted;

    private Coupon(
            String code,
            String description,
            BigDecimal discountValue,
            LocalDate expirationDate,
            boolean published
    ) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.deleted = false;
    }

    public static Coupon create(
            String code,
            String description,
            BigDecimal discountValue,
            LocalDate expirationDate,
            boolean published
    ) {
        String sanitizedCode = sanitizeCode(code);
        validateDescription(description);
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);

        return new Coupon(
                sanitizedCode,
                description.trim(),
                discountValue,
                expirationDate,
                published
        );
    }

    public void delete() {
        if (this.deleted) {
            throw new BusinessException("Coupon already deleted");
        }

        this.deleted = true;
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Description is required");
        }
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null) {
            throw new BusinessException("Discount value is required");
        }

        if (discountValue.compareTo(MIN_DISCOUNT_VALUE) < 0) {
            throw new BusinessException("Discount value must be at least 0.5");
        }
    }

    private static void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new BusinessException("Expiration date is required");
        }

        if (expirationDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }

    private static String sanitizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("Code is required");
        }

        String sanitizedCode = code.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        if (sanitizedCode.length() != CODE_LENGTH) {
            throw new BusinessException("Code must contain exactly 6 alphanumeric characters");
        }

        return sanitizedCode;
    }
}
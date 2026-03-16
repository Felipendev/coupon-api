package com.challenge.coupon.domain;

import com.challenge.coupon.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CouponTest {
    private static final String VALID_CODE = "ABC123";
    private static final String DESCRIPTION = "Test coupon";
    private static final BigDecimal VALID_DISCOUNT = new BigDecimal("1.00");
    private static final LocalDate FUTURE = LocalDate.now().plusDays(7);

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates coupon with valid data and sanitizes code")
        void createsWithSanitizedCode() {
            Coupon coupon = Coupon.create("AB-C-12-3", DESCRIPTION, VALID_DISCOUNT, FUTURE, false);

            assertThat(coupon.getCode()).isEqualTo("ABC123");
            assertThat(coupon.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo(VALID_DISCOUNT);
            assertThat(coupon.getExpirationDate()).isEqualTo(FUTURE);
            assertThat(coupon.isPublished()).isFalse();
            assertThat(coupon.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("accepts discount value exactly 0.5")
        void acceptsMinDiscount() {
            Coupon coupon = Coupon.create(VALID_CODE, DESCRIPTION, new BigDecimal("0.5"), FUTURE, false);
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo(new BigDecimal("0.5"));
        }

        @Test
        @DisplayName("throws when expiration date is in the past")
        void throwsWhenExpirationInPast() {
            LocalDate past = LocalDate.now().minusDays(1);
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, DESCRIPTION, VALID_DISCOUNT, past, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Expiration date cannot be in the past");
        }

        @Test
        @DisplayName("throws when discount value is less than 0.5")
        void throwsWhenDiscountTooLow() {
            assertThatThrownBy(() ->
                    Coupon.create(VALID_CODE, DESCRIPTION, new BigDecimal("0.4"), FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("at least");
        }

        @Test
        @DisplayName("throws when code has less than 6 alphanumeric chars after sanitization")
        void throwsWhenCodeTooShortAfterSanitization() {
            assertThatThrownBy(() -> Coupon.create("AB12", DESCRIPTION, VALID_DISCOUNT, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("6 alphanumeric");
        }

        @Test
        @DisplayName("throws when code has more than 6 alphanumeric chars after sanitization")
        void throwsWhenCodeTooLongAfterSanitization() {
            assertThatThrownBy(() -> Coupon.create("ABCDEF789", DESCRIPTION, VALID_DISCOUNT, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("exactly 6");
        }

        @Test
        @DisplayName("throws when code is null")
        void throwsWhenCodeIsNull() {
            assertThatThrownBy(() -> Coupon.create(null, DESCRIPTION, VALID_DISCOUNT, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Code is required");
        }

        @Test
        @DisplayName("throws when code is blank")
        void throwsWhenCodeIsBlank() {
            assertThatThrownBy(() -> Coupon.create("   ", DESCRIPTION, VALID_DISCOUNT, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Code is required");
        }

        @Test
        @DisplayName("throws when description is null")
        void throwsWhenDescriptionIsNull() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, null, VALID_DISCOUNT, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Description is required");
        }

        @Test
        @DisplayName("throws when description is blank")
        void throwsWhenDescriptionIsBlank() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, "  ", VALID_DISCOUNT, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Description is required");
        }

        @Test
        @DisplayName("throws when discount value is null")
        void throwsWhenDiscountValueIsNull() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, DESCRIPTION, null, FUTURE, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Discount value is required");
        }

        @Test
        @DisplayName("throws when expiration date is null")
        void throwsWhenExpirationDateIsNull() {
            assertThatThrownBy(() -> Coupon.create(VALID_CODE, DESCRIPTION, VALID_DISCOUNT, null, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Expiration date is required");
        }

        @Test
        @DisplayName("trims description when has leading/trailing spaces")
        void trimsDescription() {
            Coupon coupon = Coupon.create(VALID_CODE, "  Desc  ", VALID_DISCOUNT, FUTURE, false);
            assertThat(coupon.getDescription()).isEqualTo("Desc");
        }

        @Test
        @DisplayName("can create as published")
        void canCreatePublished() {
            Coupon coupon = Coupon.create(VALID_CODE, DESCRIPTION, VALID_DISCOUNT, FUTURE, true);
            assertThat(coupon.isPublished()).isTrue();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("sets deleted and status DELETED")
        void softDeletes() {
            Coupon coupon = Coupon.create(VALID_CODE, DESCRIPTION, VALID_DISCOUNT, FUTURE, false);
            coupon.delete();
            assertThat(coupon.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("throws when already deleted")
        void throwsWhenAlreadyDeleted() {
            Coupon coupon = Coupon.create(VALID_CODE, DESCRIPTION, VALID_DISCOUNT, FUTURE, false);
            coupon.delete();
            assertThatThrownBy(coupon::delete)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already deleted");
        }
    }
}
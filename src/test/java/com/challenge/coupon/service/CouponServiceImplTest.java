package com.challenge.coupon.service;

import com.challenge.coupon.domain.Coupon;
import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import com.challenge.coupon.exception.BusinessException;
import com.challenge.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private static final LocalDate FUTURE = LocalDate.now().plusDays(7);

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves and returns response")
        void createsAndReturnsResponse() {
            CouponRequest request = new CouponRequest(
                    "ABC-123",
                    "Desc",
                    new BigDecimal("1.0"),
                    FUTURE,
                    false
            );
            Coupon.create("ABC123", "Desc", new BigDecimal("1.0"), FUTURE, false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.<Coupon>getArgument(0));

            CouponResponse response = couponService.create(request);

            assertThat(response.code()).isEqualTo("ABC123");
            assertThat(response.description()).isEqualTo("Desc");
            assertThat(response.discountValue()).isEqualByComparingTo(new BigDecimal("1.0"));
            verify(couponRepository).save(any(Coupon.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("soft deletes when coupon exists and not deleted")
        void deletesSuccessfully() {
            UUID id = UUID.randomUUID();
            Coupon coupon = Coupon.create("ABC123", "D", new BigDecimal("0.5"), FUTURE, false);
            when(couponRepository.existsByIdAndDeletedTrue(id)).thenReturn(false);
            when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));
            when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            couponService.delete(id);

            verify(couponRepository).save(coupon);
            assertThat(coupon.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("throws when coupon already deleted")
        void throwsWhenAlreadyDeleted() {
            UUID id = UUID.randomUUID();
            when(couponRepository.existsByIdAndDeletedTrue(id)).thenReturn(true);

            assertThatThrownBy(() -> couponService.delete(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already deleted");
            verify(couponRepository).existsByIdAndDeletedTrue(id);
        }

        @Test
        @DisplayName("throws when coupon not found")
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(couponRepository.existsByIdAndDeletedTrue(id)).thenReturn(false);
            when(couponRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.delete(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not found");
        }
    }
}
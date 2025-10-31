package store.bookscamp.api.coupon.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.coupon.service.dto.CouponCreateDto;

public record CouponCreateRequest(

        @NotNull
        TargetType targetType,

        Long targetId,

        @NotNull
        DiscountType discountType,

        @NotNull
        @PositiveOrZero
        int discountValue,

        @NotNull
        @PositiveOrZero
        int minOrderAmount,

        @PositiveOrZero
        Integer maxDiscountAmount,

        @Positive
        Integer validDays
) {

    public CouponCreateDto toDto() {
        return new CouponCreateDto(
                targetType,
                targetId,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                validDays
        );
    }
}

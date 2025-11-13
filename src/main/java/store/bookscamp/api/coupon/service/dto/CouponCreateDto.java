package store.bookscamp.api.coupon.service.dto;

import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.coupon.entity.TargetType;

public record CouponCreateDto(
        TargetType targetType,
        Long targetId,
        DiscountType discountType,
        int discountValue,
        int minOrderAmount,
        Integer maxDiscountAmount,
        Integer validDays,
        String name
) {

    public Coupon toEntity() {
        return new Coupon(
                targetType,
                targetId,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                validDays,
                name
        );
    }
}

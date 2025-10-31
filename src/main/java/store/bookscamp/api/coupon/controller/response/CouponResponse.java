package store.bookscamp.api.coupon.controller.response;

import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.coupon.entity.TargetType;

public record CouponResponse(
        Long id,
        TargetType targetType,
        Long targetId,
        DiscountType discountType,
        int discountValue,
        int minOrderAmount,
        Integer maxDiscountAmount,
        Integer validDays
) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getTargetType(),
                coupon.getTargetId(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                coupon.getValidDays()
        );
    }
}

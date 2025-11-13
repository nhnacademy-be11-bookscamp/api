package store.bookscamp.api.orderinfo.controller.response;

import store.bookscamp.api.orderinfo.service.dto.CouponDto;

public record CouponInfo(
        Long couponIssueId,
        Long couponId,
        String couponName,
        String discountType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        Integer expectedDiscount
) {
    public static CouponInfo fromDto(CouponDto dto) {
        return new CouponInfo(
                dto.couponIssueId(),
                dto.couponId(),
                dto.couponName(),
                dto.discountType(),
                dto.discountValue(),
                dto.minOrderAmount(),
                dto.maxDiscountAmount(),
                dto.expectedDiscount()
        );
    }
}
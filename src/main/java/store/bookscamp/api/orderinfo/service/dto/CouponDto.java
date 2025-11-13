package store.bookscamp.api.orderinfo.service.dto;

public record CouponDto(
        Long couponIssueId,
        Long couponId,
        String couponName,
        String discountType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        Integer expectedDiscount
) {
}
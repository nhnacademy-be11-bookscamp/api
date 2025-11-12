package store.bookscamp.api.orderinfo.service.dto;

public record CouponDto(
        Long couponId,
        String couponName,
        String discountType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        Integer expectedDiscount
) {
}
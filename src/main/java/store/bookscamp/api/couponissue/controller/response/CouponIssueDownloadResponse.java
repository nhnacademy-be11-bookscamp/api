package store.bookscamp.api.couponissue.controller.response;

import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.DiscountType;

public record CouponIssueDownloadResponse(
        Long couponId,
        String name,
        String discountInfo
) {

    public static CouponIssueDownloadResponse from(Coupon coupon) {
        return new CouponIssueDownloadResponse(
                coupon.getId(),
                coupon.getName(),
                createDiscountInfo(coupon)
        );
    }

    private static String createDiscountInfo(Coupon coupon) {
        if (coupon.getDiscountType() == DiscountType.RATE) {
            String maxInfo = (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount() > 0)
                    ? " (최대 " + coupon.getMaxDiscountAmount() + "원)"
                    : "";
            return coupon.getDiscountValue() + "% 할인" + maxInfo;
        } else if (coupon.getDiscountType() == DiscountType.AMOUNT) {
            return coupon.getDiscountValue() + "원 할인";
        } else {
            return "할인 혜택";
        }
    }
}
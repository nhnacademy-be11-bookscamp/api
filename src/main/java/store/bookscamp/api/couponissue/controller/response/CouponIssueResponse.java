package store.bookscamp.api.couponissue.controller.response;

import java.time.LocalDateTime;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.couponissue.entity.CouponIssue;

public record CouponIssueResponse(
        Long couponIssueId,
        String targetType,
        String discountType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        LocalDateTime expiredAt,
        boolean isUsed,
        LocalDateTime usedAt
) {

    /**
     * CouponIssue Entity를 CouponIssueResponse 레코드로 변환하는 정적 팩토리 메서드
     */
    public static CouponIssueResponse from(CouponIssue couponIssue) {
        Coupon coupon = couponIssue.getCoupon();

        boolean usedStatus = (couponIssue.getUsedAt() != null);

        return new CouponIssueResponse(
                couponIssue.getId(),
                coupon.getTargetType().name(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                couponIssue.getExpiredAt(),
                usedStatus,
                couponIssue.getUsedAt()
        );
    }
}

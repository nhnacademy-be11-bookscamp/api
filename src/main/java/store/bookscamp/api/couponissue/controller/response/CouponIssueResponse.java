package store.bookscamp.api.couponissue.controller.response;

import java.time.LocalDateTime;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.couponissue.controller.status.CouponIssueStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;

public record CouponIssueResponse(
        Long couponIssueId,
        String targetType,
        String discountType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        LocalDateTime expiredAt,
        CouponIssueStatus status,
        LocalDateTime usedAt,
        String name
) {

    /**
     * CouponIssue Entity를 CouponIssueResponse 레코드로 변환하는 정적 팩토리 메서드
     */
    public static CouponIssueResponse from(CouponIssue couponIssue) {
        CouponIssueStatus status;
        Coupon coupon = couponIssue.getCoupon();

        if (couponIssue.getUsedAt() != null) {
            status = CouponIssueStatus.USED;
        }
        else if (couponIssue.getExpiredAt() != null && couponIssue.getExpiredAt().isBefore(LocalDateTime.now())) {
            status = CouponIssueStatus.EXPIRED;
        }
        else {
            status = CouponIssueStatus.AVAILABLE;
        }

        return new CouponIssueResponse(
                couponIssue.getId(),
                coupon.getTargetType().name(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                couponIssue.getExpiredAt(),
                status,
                couponIssue.getUsedAt(),
                coupon.getName()
        );
    }
}

package store.bookscamp.api.couponissue.controller.response;

import static store.bookscamp.api.couponissue.controller.status.CouponIssueStatus.AVAILABLE;
import static store.bookscamp.api.couponissue.controller.status.CouponIssueStatus.EXPIRED;
import static store.bookscamp.api.couponissue.controller.status.CouponIssueStatus.USED;

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

    public static CouponIssueResponse from(CouponIssue couponIssue) {
        CouponIssueStatus status;
        Coupon coupon = couponIssue.getCoupon();

        if (couponIssue.getUsedAt() != null) {
            status = USED;
        }
        else if (couponIssue.getExpiredAt() != null && couponIssue.getExpiredAt().isBefore(LocalDateTime.now())) {
            status = EXPIRED;
        }
        else {
            status = AVAILABLE;
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

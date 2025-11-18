package store.bookscamp.api.couponissue.repository.custom;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.couponissue.controller.status.CouponFilterStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;

@Repository
public interface CouponIssueRepositoryCustom {

    Page<CouponIssue> findByMemberIdAndFilterStatus(Long memberId, CouponFilterStatus status, Pageable pageable);

    List<Coupon> findDownloadableCoupons(Long memberId, Long bookId);
}
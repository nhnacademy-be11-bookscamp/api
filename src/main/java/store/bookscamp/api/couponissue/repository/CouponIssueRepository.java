package store.bookscamp.api.couponissue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.member.entity.Member;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsCouponIssueByCouponTargetTypeAndMember(TargetType targetType, Member member);
}

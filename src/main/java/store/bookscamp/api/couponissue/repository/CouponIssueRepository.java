package store.bookscamp.api.couponissue.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.member.entity.Member;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByCouponTargetTypeAndMember(TargetType targetType, Member member);

    boolean existsByCouponAndMember(Coupon coupon, Member member);

    List<CouponIssue> findAllByMember(Member member);
}

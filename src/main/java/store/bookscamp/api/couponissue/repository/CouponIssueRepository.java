package store.bookscamp.api.couponissue.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.custom.CouponIssueRepositoryCustom;
import store.bookscamp.api.member.entity.Member;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long>, CouponIssueRepositoryCustom {

    boolean existsByCouponTargetTypeAndMember(TargetType targetType, Member member);

    boolean existsByCouponAndMember(Coupon coupon, Member member);

    boolean existsByCouponAndMemberAndExpiredAtIsAfter(Coupon coupon, Member member, LocalDateTime expiredAtAfter);

    List<CouponIssue> findAllByMember(Member member);

    void deleteByMember_IdAndId(Long memberId, Long id);

    void deleteByCoupon_Id(Long couponId);
}

package store.bookscamp.api.couponissue.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import store.bookscamp.api.common.config.JpaConfig;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Import(JpaConfig.class)
@DataJpaTest
class CouponIssueRepositoryTest {

    @Autowired
    private CouponIssueRepository couponIssueRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Coupon welcomeCoupon;
    private Coupon birthdayCoupon;

    @BeforeEach
    void setUp() {
        couponIssueRepository.deleteAll();
        couponRepository.deleteAll();
        memberRepository.deleteAll();

        member = memberRepository.save(new Member(
                "회원",
                "1234",
                "member@naver.com",
                "01012345678",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member",
                LocalDateTime.now(),
                LocalDate.of(2001, 1, 1)
        ));

        welcomeCoupon = couponRepository.save(new Coupon(
                WELCOME,
                null,
                AMOUNT,
                10000,
                50000,
                10000,
                30,
                "테스트 쿠폰"
        ));

        birthdayCoupon = couponRepository.save(new Coupon(
                BIRTHDAY,
                null,
                AMOUNT,
                10000,
                50000,
                10000,
                30,
                "테스트 쿠폰"
        ));
    }

    @Test
    @DisplayName("existsByCouponTargetTypeAndMember - 특정 타입 쿠폰 발급 여부 확인")
    void existsByCouponTargetTypeAndMember() {
        couponIssueRepository.save(new CouponIssue(welcomeCoupon, member, null));

        boolean exists = couponIssueRepository.existsByCouponTargetTypeAndMember(TargetType.WELCOME, member);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCouponAndMember - 쿠폰과 회원 기준 존재 여부 확인")
    void existsByCouponAndMember() {
        couponIssueRepository.save(new CouponIssue(birthdayCoupon, member, null));

        boolean exists = couponIssueRepository.existsByCouponAndMember(birthdayCoupon, member);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCouponAndMemberAndExpiredAtIsNull - 만료되지 않은 쿠폰 존재 여부")
    void existsByCouponAndMemberAndExpiredAtIsNull() {
        couponIssueRepository.save(new CouponIssue(welcomeCoupon, member, LocalDateTime.now().plusDays(30)));

        boolean exists = couponIssueRepository.existsByCouponAndMemberAndExpiredAtIsAfter(welcomeCoupon, member, LocalDateTime.now());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findAllByMember - 회원이 가진 쿠폰 목록 조회")
    void findAllByMember() {
        couponIssueRepository.save(new CouponIssue(welcomeCoupon, member, null));
        couponIssueRepository.save(new CouponIssue(birthdayCoupon, member, null));

        var result = couponIssueRepository.findAllByMember(member);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("deleteByMember_IdAndId - 특정 회원의 쿠폰 단건 삭제")
    void deleteByMember_IdAndId() {
        CouponIssue issue = couponIssueRepository.save(new CouponIssue(welcomeCoupon, member, null));

        couponIssueRepository.deleteByMember_IdAndId(member.getId(), issue.getId());

        assertThat(couponIssueRepository.existsById(issue.getId())).isFalse();
    }

    @Test
    @DisplayName("deleteByCoupon_Id - 특정 쿠폰의 모든 Issue 삭제")
    void deleteByCoupon_Id() {
        couponIssueRepository.save(new CouponIssue(birthdayCoupon, member, null));
        couponIssueRepository.save(new CouponIssue(birthdayCoupon, member,  null));

        couponIssueRepository.deleteByCoupon_Id(birthdayCoupon.getId());

        assertThat(couponIssueRepository.count()).isZero();
    }
}

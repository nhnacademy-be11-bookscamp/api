package store.bookscamp.api.couponissue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_ISSUE_ALREADY_EXIST;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class CouponIssueServiceTest {

    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Coupon welcomeCoupon;
    private Coupon birthdayCoupon;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        couponRepository.deleteAll();
        couponIssueRepository.deleteAll();

        member = memberRepository.save(new Member(
                "회원",
                "1234",
                "member@naver.com",
                "01012345678",
                0,
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
                30)
        );

        birthdayCoupon = couponRepository.save(new Coupon(
                BIRTHDAY,
                null,
                AMOUNT,
                10000,
                50000,
                10000,
                30
        ));
    }

    @Nested
    @DisplayName("issueWelcomeCoupon 통합 테스트")
    class IssueWelcomeCoupon {

        @Test
        @DisplayName("정상적으로 웰컴 쿠폰이 발급된다.")
        void issueWelcomeCoupon_success() {

            // when
            Long issueId = couponIssueService.issueWelcomeCoupon(member.getId());

            // then
            List<CouponIssue> issuedCoupons = couponIssueRepository.findAll();

            assertThat(issuedCoupons).hasSize(1);
            assertThat(issuedCoupons.get(0).getCoupon().getTargetType()).isEqualTo(WELCOME);
            assertThat(issuedCoupons.get(0).getMember().getId()).isEqualTo(member.getId());
            assertThat(issueId).isEqualTo(issuedCoupons.get(0).getId());
        }

        @Test
        @DisplayName("이미 웰컴 쿠폰을 발급받은 회원이면 예외 발생")
        void issueWelcomeCoupon_alreadyIssued() {
            // given
            couponIssueRepository.save(new CouponIssue(welcomeCoupon, member, LocalDateTime.now().plusDays(welcomeCoupon.getValidDays())));

            // when & then
            assertThatThrownBy(() -> couponIssueService.issueWelcomeCoupon(member.getId()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(COUPON_ISSUE_ALREADY_EXIST.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 예외 발생")
        void issueWelcomeCoupon_memberNotFound() {
            // when & then
            assertThatThrownBy(() -> couponIssueService.issueWelcomeCoupon(999L))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("웰컴 쿠폰이 존재하지 않으면 예외 발생")
        void issueWelcomeCoupon_couponNotFound() {
            // given
            couponRepository.delete(welcomeCoupon);

            // when & then
            assertThatThrownBy(() -> couponIssueService.issueWelcomeCoupon(member.getId()))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(COUPON_NOT_FOUND.getMessage());
        }
    }

    @Test
    @DisplayName("생일 쿠폰을 정상적으로 발급한다")
    void issueBirthDayCoupon_success() {
        // when
        Long issueId = couponIssueService.issueBirthDayCoupon(birthdayCoupon, member);

        // then
        CouponIssue issue = couponIssueRepository.findById(issueId).orElseThrow();
        assertThat(issue.getCoupon().getId()).isEqualTo(birthdayCoupon.getId());
        assertThat(issue.getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("이미 발급된 쿠폰이 있으면 예외가 발생한다")
    void issueBirthDayCoupon_duplicate() {
        // given
        couponIssueRepository.save(new CouponIssue(birthdayCoupon, member, LocalDateTime.now().plusDays(30)));

        // when & then
        assertThatThrownBy(() ->
                couponIssueService.issueBirthDayCoupon(birthdayCoupon, member)
        )
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(COUPON_ISSUE_ALREADY_EXIST.getMessage());
    }
}

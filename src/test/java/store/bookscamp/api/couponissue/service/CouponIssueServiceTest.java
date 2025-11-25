package store.bookscamp.api.couponissue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.data.domain.PageRequest.of;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
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
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.controller.status.CouponFilterStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class CouponIssueServiceTest {

    @Autowired
    private BookRepository bookRepository;

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
                )
        );

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

    @Nested
    @DisplayName("issueGeneralCoupon 통합 테스트")
    class IssueGeneralCouponTest {

        @Test
        @DisplayName("일반 쿠폰 정상 발급")
        void issueGeneralCoupon_success() {
            // given
            Coupon coupon = couponRepository.save(new Coupon(
                    WELCOME,
                    null,
                    AMOUNT,
                    5000,
                    30000,
                    5000,
                    7,
                    "일반 쿠폰"
            ));

            // when
            Long issueId = couponIssueService.issueGeneralCoupon(coupon.getId(), member.getId());

            // then
            CouponIssue issue = couponIssueRepository.findById(issueId).orElseThrow();
            assertThat(issue.getCoupon().getId()).isEqualTo(coupon.getId());
            assertThat(issue.getMember().getId()).isEqualTo(member.getId());
        }

        @Test
        @DisplayName("이미 발급된 쿠폰이면 예외 발생")
        void issueGeneralCoupon_alreadyIssued() {
            // given
            Coupon coupon = welcomeCoupon;
            couponIssueRepository.save(new CouponIssue(
                    coupon, member,
                    LocalDateTime.now().plusDays(30))
            );

            // when & then
            assertThatThrownBy(() ->
                    couponIssueService.issueGeneralCoupon(coupon.getId(), member.getId())
            )
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(COUPON_ISSUE_ALREADY_EXIST.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰이면 예외 발생")
        void issueGeneralCoupon_couponNotFound() {
            assertThatThrownBy(() ->
                    couponIssueService.issueGeneralCoupon(9999L, member.getId())
            )
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 예외 발생")
        void issueGeneralCoupon_memberNotFound() {
            assertThatThrownBy(() ->
                    couponIssueService.issueGeneralCoupon(welcomeCoupon.getId(), 9999L)
            )
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("listCouponIssue 통합 테스트")
    class ListCouponIssueTest {

        @Test
        @DisplayName("정상적으로 조회된다")
        void listCouponIssue_success() {
            // given
            couponIssueRepository.save(new CouponIssue(welcomeCoupon, member,
                    LocalDateTime.now().plusDays(30)));

            // when
            Page<CouponIssue> page = couponIssueService.listCouponIssue(member.getId(), CouponFilterStatus.AVAILABLE,
                    of(0, 10));

            // then
            assertThat(page.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 예외 발생")
        void listCouponIssue_memberNotFound() {
            assertThatThrownBy(() ->
                    couponIssueService.listCouponIssue(9999L, null,
                            of(0, 10))
            )
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("findDownloadableCoupons 통합 테스트")
    class FindDownloadableCouponsTest {

        @Test
        @DisplayName("정상적으로 조회된다")
        void findDownloadableCoupons_success() {
            // given
            Long memberId = member.getId();

            Book book = bookRepository.save(new Book(
                    "책 제목",
                    "책 설명",
                    null,
                    "출판사",
                    LocalDate.of(2001, 1, 1),
                    "123456789012",
                    "기여자",
                    AVAILABLE,
                    false,
                    20000,
                    18000,
                    100,
                    0L
            ));

            // when
            List<Coupon> list = couponIssueService.findDownloadableCoupons(memberId, book.getId());

            // then
            assertThat(list).isNotNull();
        }


        @Test
        @DisplayName("존재하지 않는 책이면 예외 발생")
        void findDownloadableCoupons_bookNotFound() {

            assertThatThrownBy(() ->
                    couponIssueService.findDownloadableCoupons(member.getId(), 9999L)
            )
                    .isInstanceOf(ApplicationException.class)
                    .hasMessageContaining(BOOK_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteCouponIssue 통합 테스트")
    class DeleteCouponIssueTest {

        @Test
        @DisplayName("정상적으로 쿠폰 발급 내역이 삭제된다")
        void deleteCouponIssue_success() {
            // given
            CouponIssue issue = couponIssueRepository.save(
                    new CouponIssue(welcomeCoupon, member,
                            LocalDateTime.now().plusDays(30))
            );

            // when
            couponIssueService.deleteCouponIssue(member.getId(), issue.getId());

            // then
            assertThat(couponIssueRepository.findById(issue.getId())).isEmpty();
        }

        @Test
        @DisplayName("memberId는 존재하지만 해당 member의 쿠폰이 아니면 삭제되지 않는다")
        void deleteCouponIssue_notOwned() {
            // given
            Member other = memberRepository.save(new Member(
                    "다른사람", "pw", "other@naver.com", "01011112222",
                    0, null, NORMAL, LocalDate.now(), "other",
                    LocalDateTime.now(), LocalDate.of(2000, 1, 1)
            ));

            CouponIssue issue = couponIssueRepository.save(
                    new CouponIssue(welcomeCoupon, other,
                            LocalDateTime.now().plusDays(30))
            );

            // when
            couponIssueService.deleteCouponIssue(member.getId(), issue.getId());

            // then (삭제 안 됨)
            assertThat(couponIssueRepository.findById(issue.getId())).isPresent();
        }
    }
}

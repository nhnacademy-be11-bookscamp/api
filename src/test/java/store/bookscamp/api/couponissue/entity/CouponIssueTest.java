package store.bookscamp.api.couponissue.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.member.entity.Member;

class CouponIssueTest {

    private Coupon coupon;
    private Member member;

    @BeforeEach
    void setUp() {
        coupon = new Coupon(
                BOOK,
                1L,
                AMOUNT,
                1000,
                5000,
                null,
                30,
                "테스트 쿠폰"
        );

        member = new Member(
                "회원",
                "1234",
                "test@naver.com",
                "01012345678",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member",
                LocalDateTime.now(),
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    @DisplayName("use() - 최초 사용 성공 (usedAt 설정)")
    void use_success() {
        CouponIssue issue = new CouponIssue(coupon, member, LocalDateTime.now().plusDays(1));

        issue.use();

        assertThat(issue.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("use() - 이미 사용된 쿠폰이면 예외 발생")
    void use_fail_alreadyUsed() {
        CouponIssue issue = new CouponIssue(coupon, member, LocalDateTime.now().plusDays(1));

        issue.use();

        assertThatThrownBy(issue::use)
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_ALREADY_USED);
    }

    @Test
    @DisplayName("use() - 만료된 쿠폰은 예외 발생")
    void use_fail_expired() {
        CouponIssue issue = new CouponIssue(coupon, member, LocalDateTime.now().minusDays(1));

        assertThatThrownBy(issue::use)
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COUPON_EXPIRED);
    }

    @Test
    @DisplayName("restore() - usedAt null 복원")
    void restore_success() {
        CouponIssue issue = new CouponIssue(coupon, member, LocalDateTime.now().plusDays(1));

        issue.use();
        assertThat(issue.getUsedAt()).isNotNull();

        issue.restore();

        assertThat(issue.getUsedAt()).isNull();
    }
}

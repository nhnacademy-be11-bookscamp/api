package store.bookscamp.api.couponissue.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;
import static store.bookscamp.api.member.entity.MemberStatus.NORMAL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class BirthdayCouponIssueSchedulerTest {

    @Autowired
    private BirthdayCouponIssueScheduler scheduler;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        couponRepository.deleteAll();
        couponIssueRepository.deleteAll();

        coupon = couponRepository.save(new Coupon(
                BIRTHDAY,
                null,
                AMOUNT,
                10000,
                50000,
                10000,
                30
        ));

        int thisMonth = LocalDate.now().getMonthValue();

        memberRepository.save(new Member(
                "user1",
                "1234",
                "user1@naver.com",
                "01011111111",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member1",
                LocalDateTime.now(),
                LocalDate.of(2001, thisMonth, 1)
        ));

        memberRepository.save(new Member(
                "user2",
                "1234",
                "user2@naver.com",
                "01022222222",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member2",
                LocalDateTime.now(),
                LocalDate.of(1999, thisMonth, 15)
        ));

        memberRepository.save(new Member(
                "user3",
                "1234",
                "user3@naver.com",
                "01033333333",
                0,
                null,
                NORMAL,
                LocalDate.now(),
                "member3",
                LocalDateTime.now(),
                LocalDate.of(1995, thisMonth, 20)
        ));
    }

    @Test
    @DisplayName("이번 달 생일 회원들에게 생일 쿠폰을 정상 발급한다")
    void issueBirthdayCoupons_success() {
        // when
        scheduler.issueBirthdayCoupons();

        // then
        List<CouponIssue> issuedList = couponIssueRepository.findAll();
        assertThat(issuedList).hasSize(3);
        assertThat(issuedList).allMatch(issue ->
                issue.getCoupon().getTargetType() == BIRTHDAY
        );
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않으면 ApplicationException 발생")
    void couponNotFound() {
        // given
        couponRepository.deleteAll();

        // when & then
        assertThatThrownBy(() ->
                scheduler.issueBirthdayCoupons()
        ).isInstanceOf(ApplicationException.class);
    }
}

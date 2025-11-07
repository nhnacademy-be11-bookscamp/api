package store.bookscamp.api.couponissue.scheduler;

import static store.bookscamp.api.common.exception.ErrorCode.COUPON_NOT_FOUND;
import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.service.CouponIssueService;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayCouponIssueScheduler {

    private final CouponIssueService couponIssueService;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 0 1 * *")
    public void issueBirthdayCoupons() {
        log.info("생일 쿠폰 발급 시작");
        Coupon coupon = couponRepository.findByTargetType(BIRTHDAY)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
        List<Member> members = memberRepository.findAllByBirthDateMonth(LocalDate.now().getMonthValue());
        for (Member member : members) {
            try {
                couponIssueService.issueBirthDayCoupon(coupon, member);
            } catch (Exception e) {
                log.info("생일 쿠폰 발급 실패. memberId = {}", member.getId());
            }
        }
        log.info("생일 쿠폰 발급 완료");
    }
}

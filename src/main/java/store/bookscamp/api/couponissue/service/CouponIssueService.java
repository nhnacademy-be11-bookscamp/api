package store.bookscamp.api.couponissue.service;

import static java.time.LocalDate.now;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_ISSUE_ALREADY_EXIST;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponIssueService {

    private static final int DEFAULT_COUPON_VALID_DAYS = 14;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    public Long issueBirthDayCoupon(Coupon coupon, Member member) {
        CouponIssue couponIssue = new CouponIssue(coupon, member, getExpiredAt(now().getDayOfMonth()));
        return couponIssueRepository.save(couponIssue).getId();
    }

    @Transactional
    public Long issueWelcomeCoupon(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
        if (couponIssueRepository.existsCouponIssueByCouponTargetTypeAndMember(WELCOME, member)) {
            throw new ApplicationException(COUPON_ISSUE_ALREADY_EXIST);
        }

        Coupon coupon = couponRepository.findByTargetType(WELCOME)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
        CouponIssue couponIssue = new CouponIssue(coupon, member, getExpiredAt(coupon.getValidDays()));
        return couponIssueRepository.save(couponIssue).getId();
    }

    public Long issueGeneralCoupon(Coupon coupon, Member member) {
        int validDays = coupon.getValidDays() != null ? coupon.getValidDays() : DEFAULT_COUPON_VALID_DAYS;
        CouponIssue couponIssue = new CouponIssue(coupon, member, getExpiredAt(validDays));
        return couponIssueRepository.save(couponIssue).getId();
    }

    private static LocalDateTime getExpiredAt(Integer validDays) {
        return LocalDateTime.of(now().getYear(), now().getMonth(), now().getDayOfMonth(), 23, 59, 59)
                .plusDays(validDays);
    }
}

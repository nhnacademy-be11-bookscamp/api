package store.bookscamp.api.couponissue.service;

import static java.time.LocalDate.now;
import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_ISSUE_ALREADY_EXIST;
import static store.bookscamp.api.common.exception.ErrorCode.COUPON_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.couponissue.controller.status.CouponFilterStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private static final int DEFAULT_COUPON_VALID_DAYS = 14;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Retryable(noRetryFor = ApplicationException.class, backoff = @Backoff(multiplier = 2.0, maxDelay = 10000), listeners = "customRetryListener")
    @Transactional
    public Long issueBirthDayCoupon(Coupon coupon, Member member) {
        if (couponIssueRepository.existsByCouponAndMember(coupon, member)) {
            throw new ApplicationException(COUPON_ISSUE_ALREADY_EXIST);
        }
        CouponIssue couponIssue = new CouponIssue(coupon, member, getExpiredAt(now().lengthOfMonth()));
        return couponIssueRepository.save(couponIssue).getId();
    }

    @Transactional
    public Long issueWelcomeCoupon(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
        if (couponIssueRepository.existsByCouponTargetTypeAndMember(WELCOME, member)) {
            throw new ApplicationException(COUPON_ISSUE_ALREADY_EXIST);
        }

        Coupon coupon = couponRepository.findByTargetType(WELCOME)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));

        CouponIssue couponIssue = new CouponIssue(coupon, member, getExpiredAt(coupon.getValidDays()));
        return couponIssueRepository.save(couponIssue).getId();
    }

    @Transactional
    public Long issueGeneralCoupon(Long couponId, Long memberId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));

        if (couponIssueRepository.existsByCouponAndMember(coupon, member)) {
            throw new ApplicationException(COUPON_ISSUE_ALREADY_EXIST);
        }
        int validDays = coupon.getValidDays() != null ? coupon.getValidDays() : DEFAULT_COUPON_VALID_DAYS;
        CouponIssue couponIssue = new CouponIssue(coupon, member, getExpiredAt(validDays));
        return couponIssueRepository.save(couponIssue).getId();
    }

    public List<CouponIssue> listCouponIssue(Long memberId, CouponFilterStatus status) {
        if(!memberRepository.existsById(memberId)){
            throw new ApplicationException(MEMBER_NOT_FOUND);
        }

        return couponIssueRepository.findByMemberIdAndFilterStatus(memberId, status);
    }

    @Transactional(readOnly = true)
    public List<Coupon> findDownloadableCoupons(Long memberId, Long bookId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ApplicationException(MEMBER_NOT_FOUND);
        }
        if (!bookRepository.existsById(bookId)) {
            throw new ApplicationException(BOOK_NOT_FOUND);
        }

        return couponIssueRepository.findDownloadableCoupons(memberId, bookId);
    }

    @Transactional
    public void deleteCouponIssue(Long memberId, Long couponIssueId) {
        couponIssueRepository.deleteByMember_IdAndId(memberId,couponIssueId);
    }

    private static LocalDateTime getExpiredAt(Integer validDays) {
        return LocalDateTime.of(now().getYear(), now().getMonth(), now().getDayOfMonth(), 0, 0, 0)
                .plusDays(validDays);
    }
}

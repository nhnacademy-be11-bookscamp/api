package store.bookscamp.api.orderinfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.category.service.CategoryHierarchyService;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.query.CouponIssueSearchQuery;
import store.bookscamp.api.couponissue.query.dto.CouponSearchConditionDto;
import store.bookscamp.api.member.entity.Member;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class  OrderInfoService {

    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryHierarchyService categoryHierarchyService;
    private final CouponIssueSearchQuery couponIssueSearchQuery;

    public boolean isApplicableItem(Long bookId, TargetType targetType, Long targetId) {
        if (targetType == TargetType.BOOK) {
            return bookId.equals(targetId);
        }

        if (targetType == TargetType.CATEGORY) {
            return bookCategoryRepository.existsByBookIdAndCategoryId(bookId, targetId);
        }

        return false;
    }

    public int calculateItemAmount(Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BOOK_NOT_FOUND));
        return book.getSalePrice() * quantity;
    }

    public int calculateCouponDiscount(CouponIssue couponIssue, int applicableAmount) {
        Coupon coupon = couponIssue.getCoupon();

        if (coupon.getDiscountType() == DiscountType.RATE) {
            int discount = (int) Math.floor(applicableAmount * coupon.getDiscountValue() / 100.0);
            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
            return discount;
        } else if (coupon.getDiscountType() == DiscountType.AMOUNT) {
            return coupon.getDiscountValue();
        } else {
            throw new ApplicationException(ErrorCode.INVALID_COUPON_DISCOUNT_TYPE);
        }
    }

    public List<CouponIssue> getAvailableCouponIssues(Member member, List<Long> bookIds, int netAmount) {
        List<Long> allCategoryIds = categoryHierarchyService.findAllCategoryIdsIncludingParents(bookIds);

        CouponSearchConditionDto searchCondition = new CouponSearchConditionDto(
                member.getId(),
                allCategoryIds,
                bookIds
        );

        List<CouponIssue> couponIssues = couponIssueSearchQuery.searchCouponIssue(searchCondition);

        return couponIssues.stream()
                .filter(issue -> netAmount >= issue.getCoupon().getMinOrderAmount())
                .toList();
    }

    public boolean isAvailableCoupon(Long couponIssueId, Member member, List<Long> bookIds, int netAmount) {
        List<CouponIssue> availableCoupons = getAvailableCouponIssues(member, bookIds, netAmount);

        return availableCoupons.stream()
                .anyMatch(issue -> issue.getId().equals(couponIssueId));
    }
}
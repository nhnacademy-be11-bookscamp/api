package store.bookscamp.api.couponissue.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.category.service.CategoryHierarchyService;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.query.CouponIssueSearchQuery;
import store.bookscamp.api.couponissue.query.dto.CouponSearchConditionDto;
import store.bookscamp.api.member.entity.Member;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponValidationService {

    private final CouponIssueSearchQuery couponIssueSearchQuery;
    private final CategoryHierarchyService categoryHierarchyService;

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


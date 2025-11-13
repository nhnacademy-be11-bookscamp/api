package store.bookscamp.api.couponissue.query;

import static store.bookscamp.api.coupon.entity.QCoupon.coupon;
import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;
import static store.bookscamp.api.coupon.entity.TargetType.CATEGORY;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;
import static store.bookscamp.api.couponissue.entity.QCouponIssue.couponIssue;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.query.dto.CouponSearchConditionDto;

@Component
public class CouponIssueSearchQuery {

    private final JPAQueryFactory queryFactory;

    public CouponIssueSearchQuery(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 주문 시 사용할 수 있는 쿠폰 조회.
    public List<CouponIssue> searchCouponIssue(CouponSearchConditionDto dto) {
        LocalDateTime now = LocalDateTime.now();
        
        return queryFactory
                .selectFrom(couponIssue)
                .leftJoin(couponIssue.coupon, coupon).fetchJoin()
                .where(
                        couponIssue.member.id.eq(dto.memberId()),
                        couponIssue.usedAt.isNull(),
                        couponIssue.expiredAt.after(now),
                        categoryCouponIn(dto.categoryIds())
                                .or(bookCouponIn(dto.bookIds()))
                                .or(allTargetCouponIn())
                )
                .fetch();
    }

    private BooleanExpression categoryCouponIn(List<Long> categoryIds) {
        return coupon.targetType.eq(CATEGORY)
                .and(coupon.targetId.in(categoryIds));
    }

    private BooleanExpression bookCouponIn(List<Long> bookIds) {
        return coupon.targetType.eq(BOOK)
                .and(coupon.targetId.in(bookIds));
    }

    private BooleanExpression allTargetCouponIn() {
        return coupon.targetType.in(WELCOME, BIRTHDAY);
    }
}

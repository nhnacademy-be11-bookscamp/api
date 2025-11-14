package store.bookscamp.api.coupon.query;

import static store.bookscamp.api.bookcategory.entity.QBookCategory.bookCategory;
import static store.bookscamp.api.coupon.entity.QCoupon.coupon;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;
import static store.bookscamp.api.coupon.entity.TargetType.CATEGORY;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;
import store.bookscamp.api.coupon.entity.Coupon;

@Component
public class CouponSearchQuery {

    private final JPAQueryFactory queryFactory;

    public CouponSearchQuery(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 책 하나에 사용할 수 있는 책, 카테고리 쿠폰들 모두 조회
    public List<Coupon> searchCouponForBook(Long bookId) {
        return queryFactory
                .selectFrom(coupon)
                .where(
                        categoryCouponIn(bookId).or(
                        bookCouponEq(bookId))
                ).fetch();
    }

    private BooleanExpression categoryCouponIn(Long bookId) {
        return coupon.targetType.eq(CATEGORY)
                .and(coupon.targetId.in(findCategoryIdsByBookId(bookId)));
    }

    private JPQLQuery<Long> findCategoryIdsByBookId(Long bookId) {
        return JPAExpressions
                .select(bookCategory.category.id)
                .from(bookCategory)
                .where(bookCategory.book.id.eq(bookId));
    }

    private BooleanExpression bookCouponEq(Long bookId) {
        return coupon.targetType.eq(BOOK)
                .and(coupon.targetId.eq(bookId));
    }
}

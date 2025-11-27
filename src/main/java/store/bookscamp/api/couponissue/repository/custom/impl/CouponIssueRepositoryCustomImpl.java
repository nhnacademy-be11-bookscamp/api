package store.bookscamp.api.couponissue.repository.custom.impl;

import static store.bookscamp.api.coupon.entity.QCoupon.coupon;
import static store.bookscamp.api.couponissue.entity.QCouponIssue.couponIssue;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.QCoupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.couponissue.controller.status.CouponFilterStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.entity.QCouponIssue;
import store.bookscamp.api.couponissue.repository.custom.CouponIssueRepositoryCustom;

public class CouponIssueRepositoryCustomImpl implements CouponIssueRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public CouponIssueRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
        this.em = em;
    }

    @Override
    public Page<CouponIssue> findByMemberIdAndFilterStatus(Long memberId, CouponFilterStatus status,
                                                           Pageable pageable) {

        QCouponIssue qCouponIssue = couponIssue;
        LocalDateTime now = LocalDateTime.now();
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(qCouponIssue.member.id.eq(memberId));

        switch (status) {
            case AVAILABLE:
                builder.and(qCouponIssue.usedAt.isNull())
                        .and(qCouponIssue.expiredAt.isNull()
                                .or(qCouponIssue.expiredAt.goe(now)));
                break;
            case USED:
                builder.and(qCouponIssue.usedAt.isNotNull());
                break;
            case EXPIRED:
                builder.and(qCouponIssue.usedAt.isNull())
                        .and(qCouponIssue.expiredAt.isNotNull())
                        .and(qCouponIssue.expiredAt.lt(now));
                break;
            case ALL:
            default:
                break;
        }

        JPAQuery<CouponIssue> query = queryFactory
                .selectFrom(qCouponIssue)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        query = switch (status) {
            case AVAILABLE -> query.orderBy(qCouponIssue.expiredAt.asc().nullsLast());
            case USED -> query.orderBy(qCouponIssue.usedAt.desc());
            case EXPIRED -> query.orderBy(qCouponIssue.expiredAt.desc());
            default -> query.orderBy(qCouponIssue.createdAt.desc());
        };

        List<CouponIssue> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(qCouponIssue.count())
                .from(qCouponIssue)
                .where(builder);

        Long total = countQuery.fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public List<Coupon> findDownloadableCoupons(Long memberId, Long bookId) {

        QCoupon qCoupon = coupon;
        QCouponIssue qCouponIssue = couponIssue;

        String recursiveQuery = """
            
                WITH RECURSIVE CategoryAncestors (id, parent_id) AS (
                SELECT 
                    c.id, 
                    c.parent_id
                FROM 
                    category c
                JOIN 
                    book_category bc ON c.id = bc.category_id
                WHERE 
                    bc.book_id = ?1 
                
                UNION ALL
                
                SELECT 
                    p.id, 
                    p.parent_id
                FROM 
                    category p
                JOIN 
                    CategoryAncestors ca ON p.id = ca.parent_id
            )
            SELECT DISTINCT id FROM CategoryAncestors;
            """;

        Query nativeQuery = em.createNativeQuery(recursiveQuery, Long.class);
        nativeQuery.setParameter(1, bookId);

        @SuppressWarnings("unchecked")
        List<Long> allCategoryIds = (List<Long>) nativeQuery.getResultList();

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(
                qCoupon.targetType.eq(TargetType.BOOK).and(qCoupon.targetId.eq(bookId))
                        .or(
                                qCoupon.targetType.eq(TargetType.CATEGORY)
                                        .and(allCategoryIds.isEmpty() ? null : qCoupon.targetId.in(allCategoryIds))
                        )
        );

        if (memberId != null) {
            builder.and(
                    qCoupon.id.notIn(
                            JPAExpressions
                                    .select(qCouponIssue.coupon.id)
                                    .from(qCouponIssue)
                                    .where(qCouponIssue.member.id.eq(memberId))
                    )
            );
        }

        return queryFactory
                .selectFrom(qCoupon)
                .where(builder)
                .fetch();
    }
}
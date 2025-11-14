package store.bookscamp.api.couponissue.repository.custom.impl;

import static store.bookscamp.api.bookcategory.entity.QBookCategory.bookCategory;
import static store.bookscamp.api.coupon.entity.QCoupon.coupon;
import static store.bookscamp.api.couponissue.entity.QCouponIssue.couponIssue;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query; // 네이티브 쿼리용
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import store.bookscamp.api.bookcategory.entity.QBookCategory;
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
    public List<CouponIssue> findByMemberIdAndFilterStatus(Long memberId, CouponFilterStatus status) {

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
                .where(builder);

        query = switch (status) {
            case AVAILABLE -> query.orderBy(qCouponIssue.expiredAt.asc().nullsLast());
            case USED -> query.orderBy(qCouponIssue.usedAt.desc());
            case EXPIRED -> query.orderBy(qCouponIssue.expiredAt.desc());
            default -> query.orderBy(qCouponIssue.createdAt.desc());
        };

        return query.fetch();
    }

    @Override
    public List<Coupon> findDownloadableCoupons(Long memberId, Long bookId) {

        QCoupon qCoupon = coupon;
        QCouponIssue qCouponIssue = couponIssue;
        QBookCategory qBookCategory = bookCategory;

        // --- [수정] ---
        // 1. 재귀 쿼리(CTE)를 포함한 네이티브 SQL 정의
        // (주의: 'category' 테이블, 'id' 컬럼, 'parent_id' 컬럼 이름은 실제 DB에 맞게 수정해야 합니다.)
        String recursiveQuery = """
            WITH RECURSIVE CategoryAncestors (id, parent_id) AS (
                -- 1. Anchor Member: 이 책(bookId)에 직접 연결된 카테고리 ID
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
                
                -- 2. Recursive Member: 상위 부모 카테고리 찾기
                SELECT 
                    p.id, 
                    p.parent_id
                FROM 
                    category p
                JOIN 
                    CategoryAncestors ca ON p.id = ca.parent_id
            )
            -- 3. 최종적으로 모든 ID를 중복 없이 선택
            SELECT DISTINCT id FROM CategoryAncestors;
            """;

        // 2. 네이티브 쿼리 실행
        Query nativeQuery = em.createNativeQuery(recursiveQuery, Long.class); // Long.class로 타입 캐스팅
        nativeQuery.setParameter(1, bookId);

        @SuppressWarnings("unchecked")
        List<Long> allCategoryIds = (List<Long>) nativeQuery.getResultList();
        // --- [수정 끝] ---


        // 3. QueryDSL 쿼리에 네이티브 쿼리 결과(allCategoryIds)를 사용
        return queryFactory
                .select(qCoupon)
                .from(qCoupon)
                .where(
                        (
                                qCoupon.targetType.eq(TargetType.BOOK)
                                        .and(qCoupon.targetId.eq(bookId))
                        )
                                .or(
                                        // [수정] 서브쿼리 대신, 위에서 구한 allCategoryIds 목록을 사용
                                        qCoupon.targetType.eq(TargetType.CATEGORY)
                                                .and(allCategoryIds.isEmpty() ? null : qCoupon.targetId.in(allCategoryIds))
                                ),

                        // 발급여부 체크 (기존과 동일)
                        qCoupon.id.notIn(
                                JPAExpressions
                                        .select(qCouponIssue.coupon.id)
                                        .from(qCouponIssue)
                                        .where(qCouponIssue.member.id.eq(memberId))
                        )
                )
                .fetch();
    }
}
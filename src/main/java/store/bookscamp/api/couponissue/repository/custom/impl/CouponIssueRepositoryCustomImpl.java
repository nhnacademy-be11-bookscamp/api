package store.bookscamp.api.couponissue.repository.custom.impl;

import static store.bookscamp.api.bookcategory.entity.QBookCategory.*;
import static store.bookscamp.api.coupon.entity.QCoupon.*;
import static store.bookscamp.api.couponissue.entity.QCouponIssue.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.bookcategory.entity.QBookCategory;
import store.bookscamp.api.category.entity.QCategory;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.QCoupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.couponissue.controller.status.CouponFilterStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.entity.QCouponIssue;
import store.bookscamp.api.couponissue.repository.custom.CouponIssueRepositoryCustom;

@RequiredArgsConstructor
public class CouponIssueRepositoryCustomImpl implements CouponIssueRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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

        return queryFactory
                .select(qCoupon)
                .from(qCoupon)
                .where(
                        (
                                qCoupon.targetType.eq(TargetType.BOOK)
                                        .and(qCoupon.targetId.eq(bookId))
                        )
                                .or(
                                        qCoupon.targetType.eq(TargetType.CATEGORY)
                                                .and(qCoupon.targetId.in(
                                                        JPAExpressions
                                                                .select(qBookCategory.category.id)
                                                                .from(qBookCategory)
                                                                .where(qBookCategory.book.id.eq(bookId))
                                                ))
                                ),

                        // 발급여부 체크
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
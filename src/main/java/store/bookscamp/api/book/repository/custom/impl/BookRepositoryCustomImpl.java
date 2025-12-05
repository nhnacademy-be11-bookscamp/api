package store.bookscamp.api.book.repository.custom.impl;

import static store.bookscamp.api.booklike.entity.QBookLike.bookLike;
import static store.bookscamp.api.orderitem.entity.QOrderItem.orderItem;
import static store.bookscamp.api.review.entity.QReview.review;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;

public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public BookRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private static final QBook book = QBook.book;

    @Override
    public Page<Book> getBooks(String keyword, Pageable pageable) {

        BooleanExpression condition = (keyword != null && !keyword.isEmpty())
                ? book.title.containsIgnoreCase(keyword)
                : null;

        List<Book> content = queryFactory
                .selectFrom(book)
                .where(condition)
                .orderBy(book.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Book> getRecommendBooks() {
        return queryFactory
                .selectFrom(book)
                .orderBy(book.viewCount.desc())
                .limit(12)
                .fetch();
    }

    @Override
    public Page<Book> getNewBooks(Pageable pageable){
        List<Book> content = queryFactory
                .selectFrom(book)
                .where(book.publishDate.after(LocalDate.now().minusMonths(1)))
                .orderBy(book.publishDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(book.publishDate.after(LocalDate.now().minusMonths(1)))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Book> getBestSellers(Pageable pageable) {

        long maxLimit = 100L;

        if (pageable.getOffset() >= maxLimit) {
            return new PageImpl<>(List.of(), pageable, maxLimit);
        }

        NumberExpression<Double> totalScore = calculateTotalScore();

        long limitForQuery = Math.min(pageable.getPageSize(), maxLimit - pageable.getOffset());

        List<Book> content = queryFactory
                .selectFrom(book)
                .where(book.status.eq(BookStatus.AVAILABLE)
                        .or(book.status.eq(BookStatus.SOLD_OUT)))
                .orderBy(totalScore.desc())
                .offset(pageable.getOffset())
                .limit(limitForQuery)
                .fetch();

        Long actualTotal = queryFactory
                .select(book.count())
                .from(book)
                .where(book.status.eq(BookStatus.AVAILABLE)
                        .or(book.status.eq(BookStatus.SOLD_OUT)))
                .fetchOne();

        long effectiveTotal = actualTotal != null ? Math.min(actualTotal, maxLimit) : 0L;

        return new PageImpl<>(content, pageable, effectiveTotal);
    }

    private NumberExpression<Double> calculateTotalScore() {

        NumberExpression<Double> salesScore = Expressions.asNumber(
                JPAExpressions.select(orderItem.orderQuantity.sum().coalesce(0))
                        .from(orderItem)
                        .where(orderItem.book.eq(book))
        ).castToNum(Double.class).multiply(50);

        NumberExpression<Double> likeScore = Expressions.asNumber(
                JPAExpressions.select(bookLike.count())
                        .from(bookLike)
                        .where(bookLike.book.eq(book).and(bookLike.liked.isTrue()))
        ).castToNum(Double.class).multiply(10);

        NumberExpression<Double> avgScore = Expressions.asNumber(
                JPAExpressions.select(review.score.avg().coalesce(0.0))
                        .from(review)
                        .join(review.orderItem, orderItem)
                        .where(orderItem.book.eq(book))
        ).castToNum(Double.class).multiply(20);

        NumberExpression<Double> viewScore = book.viewCount
                .castToNum(Double.class)
                .multiply(1);

        return salesScore.add(likeScore).add(avgScore).add(viewScore);
    }
}


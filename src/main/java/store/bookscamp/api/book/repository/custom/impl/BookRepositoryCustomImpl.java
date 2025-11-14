package store.bookscamp.api.book.repository.custom.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;
import store.bookscamp.api.bookcategory.entity.QBookCategory;
import store.bookscamp.api.booklike.entity.QBookLike;
import store.bookscamp.api.category.entity.QCategory;

public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public BookRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private static final QBook book = QBook.book;
    private static final QBookCategory bookCategory = QBookCategory.bookCategory;
    private static final QCategory category = QCategory.category;
    private static final QBookLike bookLike = QBookLike.bookLike;

    @Override
    public Page<Book> getBooks(List<Long> categoryIds, String sortType, Pageable pageable) {

        OrderSpecifier<?> sortOrder = getSortSpecifier(sortType, book);

        List<Book> results = queryFactory
                .select(book)
                .from(book)
                .leftJoin(bookCategory).on(book.id.eq(bookCategory.book.id))
                .leftJoin(category).on(bookCategory.category.id.eq(category.id))
                .leftJoin(bookLike).on(book.id.eq(bookLike.book.id).and(bookLike.liked.isTrue()))
                .where(inCategories(categoryIds))
                .groupBy(book.id)
                .orderBy(sortOrder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(book.countDistinct())
                .from(book)
                .leftJoin(bookCategory).on(book.id.eq(bookCategory.book.id))
                .leftJoin(category).on(bookCategory.category.id.eq(category.id))
                .leftJoin(bookLike).on(book.id.eq(bookLike.book.id).and(bookLike.liked.isTrue()))
                .where(inCategories(categoryIds))
                .fetchOne();

        long total = (totalCount != null) ? totalCount : 0L;

        return new PageImpl<>(results, pageable, total);
    }


    private BooleanExpression inCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return category.id.in(categoryIds);
    }

    // 케이스에 따라 orderBy 안의 조건을 선택하는 역할
    private OrderSpecifier<?> getSortSpecifier(String sortType, QBook book) {
        return switch (sortType) {
            case "title" -> book.title.asc();
            case "low-view" -> book.viewCount.asc();
            case "high-view" -> book.viewCount.desc();
            case "low-price" -> book.salePrice.asc();
            case "high-price" -> book.salePrice.desc();
            case "publishDate" -> book.publishDate.desc();
            case "bookLike" -> bookLike.id.count().desc();
            default -> book.id.asc();
        };
    }

    public List<Book> getRecommendBooks() {
        return queryFactory
                .selectFrom(book)
                .orderBy(book.viewCount.desc())
                .limit(12)
                .fetch();
    }
}


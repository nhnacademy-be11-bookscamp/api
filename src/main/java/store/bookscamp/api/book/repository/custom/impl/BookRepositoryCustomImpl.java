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
    private static final QCategory category = QCategory.category;
    private static final QBookLike bookLike = QBookLike.bookLike;

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

    public List<Book> getRecommendBooks() {
        return queryFactory
                .selectFrom(book)
                .orderBy(book.viewCount.desc())
                .limit(12)
                .fetch();
    }
}


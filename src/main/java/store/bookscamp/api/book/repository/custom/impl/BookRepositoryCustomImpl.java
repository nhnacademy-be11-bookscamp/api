package store.bookscamp.api.book.repository.custom.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.entity.Book;
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
}


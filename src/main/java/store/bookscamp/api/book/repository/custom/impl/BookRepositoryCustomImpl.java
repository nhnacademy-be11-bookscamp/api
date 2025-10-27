package store.bookscamp.api.book.repository.custom.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;
import store.bookscamp.api.bookcategory.entity.QBookCategory;
import store.bookscamp.api.category.entity.QCategory;
import store.bookscamp.api.contributor.entity.QContributor;

@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBook book = QBook.book;
    private final QBookCategory bookCategory = QBookCategory.bookCategory;
    private final QCategory category = QCategory.category;
    private final QContributor contributor = QContributor.contributor;

    @Override
    public Page<Book> getBooks(List<Long> categoryIds, String keyword, String sortType, Pageable pageable) {

        OrderSpecifier<?> sortOrder = getSortSpecifier(sortType, book);

        // where 조건에 만족하는 책의 정보를 가져오는 역할
        List<Book> results = queryFactory
                .selectFrom(book)
                .leftJoin(book.contributor, contributor).fetchJoin()
                .leftJoin(bookCategory).on(book.id.eq(bookCategory.book.id))
                .leftJoin(category).on(bookCategory.category.id.eq(category.id))
                .where(
                        inCategories(categoryIds)
                )
                .orderBy(sortOrder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // where 조건에 만족하는 책의 수량을 카운트 하는 역할
        Long totalCount = queryFactory
                .select(book.count())
                .from(book)
                .leftJoin(book.contributor, contributor)
                .leftJoin(bookCategory).on(book.id.eq(bookCategory.book.id))
                .leftJoin(category).on(bookCategory.category.id.eq(category.id))
                .where(
                        inCategories(categoryIds)
                )
                .fetchOne();

        long total = (totalCount != null) ? totalCount : 0L;

        return new PageImpl<>(results, pageable, total);
    }


    private BooleanExpression inCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null; // 카테고리 필터 없음
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
            default -> book.id.asc();
        };
    }
}


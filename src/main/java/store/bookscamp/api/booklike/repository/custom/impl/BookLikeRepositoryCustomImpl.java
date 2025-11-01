package store.bookscamp.api.booklike.repository.custom.impl;

import static store.bookscamp.api.book.entity.QBook.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.booklike.entity.QBookLike;
import store.bookscamp.api.booklike.repository.custom.BookLikeRepositoryCustom;

@RequiredArgsConstructor
public class BookLikeRepositoryCustomImpl implements BookLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBook book = QBook.book;
    private final QBookLike bookLike = QBookLike.bookLike;

    @Override
    public long getNumberOfLikes(Long bookId) {

        Long likeCount = queryFactory
                .select(bookLike.count())
                .from(bookLike)
                .where(
                        bookLike.liked.isTrue(),
                        bookLike.book.id.eq(bookId)
                ).fetchOne();

        return (likeCount != null) ? likeCount : 0L;
    }
}

package store.bookscamp.api.review.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store.bookscamp.api.book.entity.QBook;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.entity.QOrderInfo;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.entity.QOrderItem;
import store.bookscamp.api.review.entity.QReview;
import store.bookscamp.api.review.service.dto.MyReviewDto;
import store.bookscamp.api.review.service.dto.ReviewableItemDto;
import store.bookscamp.api.reviewimage.entity.QReviewImage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final BookImageService bookImageService;

    public List<ReviewableItemDto> findReviewableItems(Long memberId) {

        QOrderItem oi = QOrderItem.orderItem;
        QOrderInfo info = QOrderInfo.orderInfo;
        QBook book = QBook.book;
        QReview review = QReview.review;

        List<OrderItem> items = queryFactory
                .select(oi)
                .from(oi)
                .join(oi.orderInfo, info).fetchJoin()
                .join(oi.book, book).fetchJoin()
                .where(
                        info.member.id.eq(memberId),
                        info.orderStatus.eq(OrderStatus.DELIVERED),
                        oi.id.notIn(
                                JPAExpressions
                                        .select(review.orderItem.id)
                                        .from(review)
                                        .where(review.member.id.eq(memberId))
                        )
                )
                .orderBy(info.createdAt.desc())
                .fetch();

        // 여기서 thumbnailUrl 을 Service 계층에서 붙여줌
        return items.stream()
                .map(o -> new ReviewableItemDto(
                        o.getId(),
                        o.getBook().getId(),
                        o.getBook().getTitle(),
                        bookImageService.getThumbnailUrl(o.getBook().getId())
                ))
                .toList();
    }

    public List<MyReviewDto> findMyReviews(Long memberId) {

        QReview review = QReview.review;
        QOrderItem oi = QOrderItem.orderItem;
        QBook book = QBook.book;
        QReviewImage img = QReviewImage.reviewImage;

        // 리뷰 기본 정보 조회
        List<Tuple> results = queryFactory
                .select(
                        review.id,
                        book.id,
                        book.title,
                        review.content,
                        review.score,
                        review.createdAt
                )
                .from(review)
                .join(review.orderItem, oi)
                .join(oi.book, book)
                .where(review.member.id.eq(memberId))
                .orderBy(review.createdAt.desc())
                .fetch();

        // 상세 매핑
        return results.stream()
                .map(tuple -> {

                    Long reviewId = tuple.get(review.id);
                    Long bookId = tuple.get(book.id);

                    // 썸네일 추출
                    String thumbnailUrl = bookImageService.getThumbnailUrl(bookId);

                    // 리뷰 이미지 목록
                    List<String> images = queryFactory
                            .select(img.imageUrl)
                            .from(img)
                            .where(img.review.id.eq(reviewId))
                            .fetch();

                    return new MyReviewDto(
                            reviewId,
                            bookId,
                            tuple.get(book.title),
                            thumbnailUrl,
                            tuple.get(review.content),
                            tuple.get(review.score),
                            tuple.get(review.createdAt),
                            images
                    );
                })
                .toList();
    }
}

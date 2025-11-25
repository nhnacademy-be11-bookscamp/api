package store.bookscamp.api.review.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookimage.service.BookImageService;

import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.repository.MemberRepository;

import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;

import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.review.repository.ReviewQueryRepository;
import store.bookscamp.api.review.repository.ReviewRepository;
import store.bookscamp.api.review.service.dto.MyReviewDto;
import store.bookscamp.api.review.service.dto.ReviewableItemDto;

@SpringBootTest
@Transactional
class ReviewQueryRepositoryTest {

    @Autowired
    private ReviewQueryRepository reviewQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockitoBean
    private BookImageService bookImageService;

    @Autowired
    private EntityManager em;

    private Member createMember(String emailSuffix) {
        return memberRepository.save(new Member(
                "tester",
                "pass",
                "user" + emailSuffix + "@test.com",
                "010" + (int)(Math.random() * 10000000),
                0,
                null,
                MemberStatus.NORMAL,
                LocalDate.now(),
                "username" + emailSuffix,
                LocalDateTime.now(),
                LocalDate.of(1999, 1, 1)
        ));
    }

    private Book createBook(String title) {
        return bookRepository.save(new Book(
                title,
                "설명",
                null,
                "출판사",
                LocalDate.of(2023, 1, 1),
                "1111111111111",
                "저자",
                BookStatus.AVAILABLE,
                false,
                15000,
                13000,
                10,
                0L
        ));
    }

    private OrderInfo createOrderInfo(Member member, OrderStatus status) {
        return orderInfoRepository.save(
                new OrderInfo(
                        "ORDER-" + System.nanoTime(),
                        member,
                        null,
                        null,
                        10000,
                        12000,
                        3000,
                        1000,
                        0,
                        12000,
                        status,
                        0
                )
        );
    }

    private OrderItem createOrderItem(OrderInfo info, Book book) {
        return orderItemRepository.save(new OrderItem(
                info,
                null,
                book,
                1,
                15000,
                15000
        ));
    }

    @Test
    @DisplayName("리뷰 가능한 주문 조회 성공 - 배송완료 + 리뷰 없음")
    void findReviewableItems_success() {

        Member member = createMember("A");
        Book book = createBook("리뷰안된책");
        OrderInfo info = createOrderInfo(member, OrderStatus.DELIVERED);
        OrderItem item = createOrderItem(info, book);

        when(bookImageService.getThumbnailUrl(book.getId()))
                .thenReturn("thumb-url");

        List<ReviewableItemDto> list =
                reviewQueryRepository.findReviewableItems(member.getId());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).bookId()).isEqualTo(book.getId());
        assertThat(list.get(0).thumbnailUrl()).isEqualTo("thumb-url");
    }

    @Test
    @DisplayName("리뷰 가능한 주문 조회 - 리뷰 이미 작성한 경우 제외")
    void findReviewableItems_reviewExists_excluded() {

        Member member = createMember("B");
        Book book = createBook("이미리뷰함");
        OrderInfo info = createOrderInfo(member, OrderStatus.DELIVERED);
        OrderItem item = createOrderItem(info, book);

        reviewRepository.save(new Review(item, member, "내용", 5));

        List<ReviewableItemDto> list =
                reviewQueryRepository.findReviewableItems(member.getId());

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("리뷰 가능한 주문 조회 - 배송완료가 아니면 제외")
    void findReviewableItems_notDelivered_excluded() {

        Member member = createMember("C");
        Book book = createBook("배송미완료");
        OrderInfo info = createOrderInfo(member, OrderStatus.PENDING);
        createOrderItem(info, book);

        List<ReviewableItemDto> list =
                reviewQueryRepository.findReviewableItems(member.getId());

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("내 리뷰 조회 성공")
    void findMyReviews_success() {

        Member member = createMember("D");
        Book book = createBook("내책");
        OrderInfo info = createOrderInfo(member, OrderStatus.DELIVERED);
        OrderItem item = createOrderItem(info, book);

        reviewRepository.save(new Review(item, member, "리뷰내용", 4));

        when(bookImageService.getThumbnailUrl(book.getId()))
                .thenReturn("thumb-url");

        List<MyReviewDto> list =
                reviewQueryRepository.findMyReviews(member.getId());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).content()).isEqualTo("리뷰내용");
        assertThat(list.get(0).thumbnailUrl()).isEqualTo("thumb-url");
    }

    @Test
    @DisplayName("내 리뷰가 없으면 빈 리스트 반환")
    void findMyReviews_noReview() {

        Member member = createMember("E");

        List<MyReviewDto> list =
                reviewQueryRepository.findMyReviews(member.getId());

        assertThat(list).isEmpty();
    }
}

package store.bookscamp.api.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static store.bookscamp.api.common.exception.ErrorCode.NO_PERMISSION;
import static store.bookscamp.api.common.exception.ErrorCode.REVIEW_ALREADY_EXISTS;
import static store.bookscamp.api.common.exception.ErrorCode.POINT_POLICY_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.BookIndexService;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.entity.MemberStatus;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.entity.RewardType;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.review.repository.ReviewQueryRepository;
import store.bookscamp.api.review.repository.ReviewRepository;
import store.bookscamp.api.review.service.dto.MyReviewDto;
import store.bookscamp.api.review.service.dto.ReviewCreateDto;
import store.bookscamp.api.review.service.dto.ReviewUpdateDto;
import store.bookscamp.api.review.service.dto.ReviewableItemDto;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @MockitoBean
    private ReviewQueryRepository reviewQueryRepository;

    @MockitoBean
    private BookIndexService bookIndexService;

    @MockitoBean
    private BookImageService bookImageService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PointPolicyRepository pointPolicyRepository;

    private Member createMember() {
        long suffix = System.nanoTime(); // 항상 다른 값

        return memberRepository.save(
                new Member(
                        "tester",
                        "pass",
                        "test" + suffix + "@test.com",
                        "010" + (int)(Math.random()*100000000),
                        0,
                        null,
                        MemberStatus.NORMAL,
                        LocalDate.now(),
                        "user" + suffix,                          // ★ 랜덤 username
                        LocalDateTime.now(),
                        LocalDate.of(1995, 1, 1)
                )
        );
    }

    private Book createBook() {
        return bookRepository.save(
                new Book(
                        "책 제목",
                        "설명",
                        null,
                        "출판사",
                        LocalDate.of(2020, 1, 1),
                        "1234567890123",
                        "저자",
                        BookStatus.AVAILABLE,
                        false,
                        20000,
                        18000,
                        10,
                        0L
                )
        );
    }

    private OrderItem createOrderItem(Member member, Book book) {

        return orderItemRepository.save(
                new OrderItem(
                        null,       // OrderInfo
                        null,       // Packaging
                        book,       // Book
                        1,          // orderQuantity
                        18000,      // orderPrice
                        18000       // totalAmount
                )
        );
    }


    // ============================================================
    // 리뷰 생성
    // ============================================================

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_success() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        pointPolicyRepository.save(new PointPolicy(PointPolicyType.REVIEW_IMAGE, RewardType.AMOUNT, 100));

        ReviewCreateDto dto = new ReviewCreateDto(
                orderItem.getId(),
                member.getId(),
                5,
                "리뷰 내용",
                List.of("img1")
        );

        // when
        reviewService.createReview(dto);


        // then
        Review saved = reviewRepository.findByOrderItemAndMember(orderItem, member);
        assertThat(saved.getContent()).isEqualTo("리뷰 내용");
        assertThat(saved.getScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("이미 리뷰가 있으면 REVIEW_ALREADY_EXISTS 발생")
    void createReview_duplicate_fail() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        reviewRepository.save(new Review(orderItem, member, "이미 있음", 4));

        ReviewCreateDto dto = new ReviewCreateDto(
                orderItem.getId(),
                member.getId(),
                5,
                "새 리뷰",
                List.of()
        );

        // expect
        assertThatThrownBy(() -> reviewService.createReview(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(REVIEW_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("포인트 정책 없으면 POINT_POLICY_NOT_FOUND 발생")
    void createReview_noPolicy_fail() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        // REVIEW_TEXT 정책 저장 안함
        ReviewCreateDto dto = new ReviewCreateDto(
                orderItem.getId(),
                member.getId(),
                4,
                "리뷰",
                List.of()
        );

        // expect
        assertThatThrownBy(() -> reviewService.createReview(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(POINT_POLICY_NOT_FOUND.getMessage());
    }


    // ============================================================
    // 리뷰 수정
    // ============================================================

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        Review review = reviewRepository.save(new Review(orderItem, member, "old", 3));

        ReviewUpdateDto dto = new ReviewUpdateDto(
                review.getId(),
                member.getId(),
                5,
                "new content",
                List.of("img1"),
                List.of()
        );

        // when
        reviewService.updateReview(dto);

        // then
        Review updated = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("new content");
        assertThat(updated.getScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("내 리뷰가 아니면 NO_PERMISSION 발생")
    void updateReview_noPermission_fail() {

        // given
        Member owner = createMember();
        Member other = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(owner, book);

        Review review = reviewRepository.save(new Review(orderItem, owner, "내용", 4));

        ReviewUpdateDto dto = new ReviewUpdateDto(
                review.getId(),
                other.getId(),
                5,
                "new",
                List.of(),
                List.of()
        );

        // expect
        assertThatThrownBy(() -> reviewService.updateReview(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(NO_PERMISSION.getMessage());
    }


    // ============================================================
    // 리뷰 조회
    // ============================================================

    @Test
    @DisplayName("리뷰 가능 상품 조회 성공")
    void getReviewableItems_success() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        when(reviewQueryRepository.findReviewableItems(member.getId()))
                .thenReturn(List.of(new ReviewableItemDto(
                        orderItem.getId(),
                        book.getId(),
                        book.getTitle(),
                        "thumb-url"
                )));

        // when
        List<ReviewableItemDto> items = reviewService.getReviewableItems(member.getId());

        // then
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).bookId()).isEqualTo(book.getId());
    }

    @Test
    @DisplayName("내 리뷰 목록 조회 성공")
    void getMyReviews_success() {

        // given
        Member member = createMember();

        when(reviewQueryRepository.findMyReviews(member.getId()))
                .thenReturn(
                        List.of(new MyReviewDto(
                                1L,
                                1L,
                                "책 제목",
                                "thumb",
                                "내용",
                                5,
                                LocalDateTime.now(),
                                List.of()
                        ))
                );

        // when
        List<MyReviewDto> list = reviewService.getMyReviews(member.getId());

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).content()).isEqualTo("내용");
    }


    @Test
    @DisplayName("리뷰 수정 페이지 조회 성공")
    void getUpdateReview_success() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        Review review = reviewRepository.save(new Review(orderItem, member, "내용", 4));

        // when
        MyReviewDto dto = reviewService.getUpdateReview(review.getId(), member.getId());

        // then
        assertThat(dto.reviewId()).isEqualTo(review.getId());
        assertThat(dto.bookId()).isEqualTo(book.getId());
        assertThat(dto.content()).isEqualTo("내용");
        assertThat(dto.score()).isEqualTo(4);
    }


    // ============================================================
    // 평균 평점
    // ============================================================

    @Test
    @DisplayName("평균 평점 조회 성공")
    void getReviewAverageScore_success() {

        // given
        Member member = createMember();
        Book book = createBook();
        OrderItem orderItem = createOrderItem(member, book);

        reviewRepository.save(new Review(orderItem, member, "리뷰", 5));

        // when
        Double avg = reviewService.getReviewAverageScore(book.getId());

        // then
        assertThat(avg).isEqualTo(5.0);
    }

    @Test
    @DisplayName("리뷰 없으면 평균 평점 0.0 반환")
    void getReviewAverageScore_noReview() {

        // given
        Book book = createBook();

        // when
        Double avg = reviewService.getReviewAverageScore(book.getId());

        // then
        assertThat(avg).isEqualTo(0.0);
    }
}

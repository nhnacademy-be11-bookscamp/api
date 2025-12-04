package store.bookscamp.api.review.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.review.repository.ReviewRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AiReviewServiceTest {

    @Autowired
    private AiReviewService aiReviewService;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @MockitoBean
    private BookRepository bookRepository;

    private Book createBook() {
        Book b = new Book(
                "도서 제목",
                "설명",
                null,
                "출판사",
                LocalDate.of(2020, 1, 1),
                "1111222233334",
                "저자",
                BookStatus.AVAILABLE,
                false,
                20000,
                18000,
                10,
                0L
        );
        b.setId(1L);
        return b;
    }

    private Review createReview(int score, String content) {
        Review r = new Review(null, null, content, score);
        r.setId((long) (Math.random() * 10000));
        return r;
    }

    @Test
    @DisplayName("selectReviews - 리뷰 5개 미만이면 빈 리스트")
    void selectReviews_underFive_returnsEmpty() {
        List<Review> reviews = List.of(
                createReview(5, "A"),
                createReview(4, "B")
        );

        List<Review> result = invokeSelectReviews(reviews);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("selectReviews - 점수 순서대로 5개 선택됨")
    void selectReviews_topFive_success() {

        List<Review> reviews = List.of(
                createReview(5, "A"),
                createReview(5, "B"),
                createReview(4, "C"),
                createReview(4, "D"),
                createReview(3, "E"),
                createReview(3, "F")
        );

        List<Review> result = invokeSelectReviews(reviews);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getScore()).isEqualTo(5);
        assertThat(result.get(4).getScore()).isEqualTo(3);
    }

    @SuppressWarnings("unchecked")
    private List<Review> invokeSelectReviews(List<Review> reviews) {
        try {
            var m = AiReviewService.class.getDeclaredMethod("selectReviews", List.class);
            m.setAccessible(true);
            return (List<Review>) m.invoke(aiReviewService, reviews);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("buildPrompt - 정상 생성")
    void buildPrompt_success() throws Exception {
        Book book = createBook();
        List<Review> reviews = List.of(
                createReview(5, "좋아요"),
                createReview(4, "괜찮아요")
        );

        var m = AiReviewService.class.getDeclaredMethod("buildPrompt", Book.class, List.class);
        m.setAccessible(true);
        String prompt = (String) m.invoke(aiReviewService, book, reviews);

        assertThat(prompt).contains("도서 제목");
        assertThat(prompt).contains("좋아요");
    }

    @Test
    @DisplayName("callGeminiApi - 정상 응답 반환")
    void callGeminiApi_success() throws Exception {

        AiReviewService spyService = Mockito.spy(aiReviewService);

        String mockJson = """
    {
        "candidates": [
            {
                "content": {
                    "parts": [
                        {"text": "AI 리뷰 결과"}
                    ]
                }
            }
        ]
    }
    """;

        doReturn(mockJson).when(spyService)
                .executeGeminiRequest(anyString());

        assertThat(spyService.callGeminiApi("프롬프트"))
                .isEqualTo("AI 리뷰 결과");
    }

    @Test
    @DisplayName("callGeminiApi - candidates 없음 → null 반환")
    void callGeminiApi_emptyCandidates_returnsNull() throws Exception {

        AiReviewService spyService = Mockito.spy(aiReviewService);

        String mockJson = """
    {
        "candidates": []
    }
    """;

        doReturn(mockJson).when(spyService)
                .executeGeminiRequest(anyString());

        assertThat(spyService.callGeminiApi("프롬프트")).isNull();
    }

    @Test
    @DisplayName("updateAiReviewForBook - 리뷰 부족 → aiReview null 저장")
    void updateAiReviewForBook_noEnoughReviews() {

        Book book = createBook();

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(reviewRepository.findAiReviewsByBookId(book.getId()))
                .thenReturn(List.of(createReview(5, "A")));

        AiReviewService spyService = Mockito.spy(aiReviewService);

        spyService.updateAiReviewForBook(book);

        assertThat(book.getAiReview()).isNull();
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("updateAiReviewForBook - 정상 AI 리뷰 생성")
    void updateAiReviewForBook_success() {

        Book book = createBook();
        List<Review> reviews = List.of(
                createReview(5, "A"),
                createReview(5, "B"),
                createReview(4, "C"),
                createReview(4, "D"),
                createReview(3, "E")
        );

        when(reviewRepository.findAiReviewsByBookId(book.getId()))
                .thenReturn(reviews);

        AiReviewService spyService = Mockito.spy(aiReviewService);

        doReturn("AI 요약 리뷰").when(spyService).callGeminiApi(anyString());

        spyService.updateAiReviewForBook(book);

        assertThat(book.getAiReview()).isEqualTo("AI 요약 리뷰");
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("getBookById - 성공")
    void getBookById_success() {

        Book book = createBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book found = aiReviewService.getBookById(1L);

        assertThat(found).isEqualTo(book);
    }

    @Test
    @DisplayName("getBookById - BOOK_NOT_FOUND 예외")
    void getBookById_notFound() {

        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiReviewService.getBookById(1L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.BOOK_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("callGeminiApi - 예외 발생 → ApplicationException")
    void callGeminiApi_exception_throw() throws Exception {

        AiReviewService spyService = Mockito.spy(aiReviewService);

        doThrow(new RuntimeException("fail"))
                .when(spyService)
                .executeGeminiRequest(anyString());

        assertThatThrownBy(() -> spyService.callGeminiApi("프롬프트"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.AI_REVIEW_API_ERROR.getMessage());
    }

}

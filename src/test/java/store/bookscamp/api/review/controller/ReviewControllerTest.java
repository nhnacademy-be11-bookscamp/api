package store.bookscamp.api.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.review.controller.request.ReviewCreateRequest;
import store.bookscamp.api.review.controller.request.ReviewUpdateRequest;
import store.bookscamp.api.review.service.AiReviewService;
import store.bookscamp.api.review.service.ReviewService;
import store.bookscamp.api.review.service.dto.BookReviewDto;
import store.bookscamp.api.review.service.dto.MyReviewDto;
import store.bookscamp.api.review.service.dto.ReviewCreateDto;
import store.bookscamp.api.review.service.dto.ReviewUpdateDto;
import store.bookscamp.api.review.service.dto.ReviewableItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReviewService reviewService;

    @MockitoBean
    AiReviewService aiReviewService;

    @Test
    @DisplayName("리뷰 가능 상품 조회 성공")
    void getReviewableItems_success() throws Exception {

        List<ReviewableItemDto> list = List.of(
                new ReviewableItemDto(1L, 2L, "책1", "thumb1"),
                new ReviewableItemDto(2L, 3L, "책2", "thumb2")
        );

        given(reviewService.getReviewableItems(1L)).willReturn(list);

        mockMvc.perform(get("/member/review/reviewable")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(2L))
                .andExpect(jsonPath("$[1].orderItemId").value(2L));
    }

    @Test
    @DisplayName("내 리뷰 조회 성공")
    void getMyReviews_success() throws Exception {

        List<MyReviewDto> list = List.of(
                new MyReviewDto(
                        1L, 2L, "책1", "thumb",
                        "좋음", 5, LocalDateTime.now(), List.of("img1")
                        ));

        given(reviewService.getMyReviews(1L)).willReturn(list);

        mockMvc.perform(get("/member/review/my")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(1L))
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    @DisplayName("리뷰 수정 페이지 데이터 조회 성공")
    void getUpdateReview_success() throws Exception {

        MyReviewDto dto = new MyReviewDto(
                1L, 2L, "책1", "thumb",
                "내용", 4, LocalDateTime.now(), List.of("a.png")
        );

        given(reviewService.getUpdateReview(1L, 1L)).willReturn(dto);

        mockMvc.perform(get("/member/review/{reviewId}", 1L)
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L));
    }

    @Test
    @DisplayName("도서 리뷰 목록 조회 성공")
    void getBookReviews_success() throws Exception {

        BookReviewDto dto1 = new BookReviewDto(1L, "user1", "좋음",
                5, LocalDateTime.now(), List.of("a"));
        BookReviewDto dto2 = new BookReviewDto(2L, "user2", "괜찮음",
                4, LocalDateTime.now(), List.of());

        Page<BookReviewDto> page = new org.springframework.data.domain.PageImpl<>(
                List.of(dto1, dto2)
        );

        given(reviewService.getBookReviews(eq(10L), any())).willReturn(page);

        mockMvc.perform(get("/review/book/10")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reviewId").value(1L))
                .andExpect(jsonPath("$.content[1].score").value(4));
    }

    @Test
    @DisplayName("도서 평균 평점 조회 성공")
    void getBookAvgScore_success() throws Exception {

        given(reviewService.getReviewAverageScore(10L)).willReturn(4.5);

        mockMvc.perform(get("/review/book/10/avg"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReview_success() throws Exception {

        ReviewCreateRequest request = new ReviewCreateRequest(
                10L,
                5,
                "좋은 책입니다",
                List.of("img1.png")
        );

        mockMvc.perform(post("/member/review")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewService).createReview(any(ReviewCreateDto.class));
    }


    @Test
    @DisplayName("리뷰 등록 실패 - score null")
    void createReview_fail_scoreNull() throws Exception {

        ReviewCreateRequest request = new ReviewCreateRequest(
                1L,
                null,
                "내용",
                List.of()
        );

        mockMvc.perform(post("/member/review")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() throws Exception {

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                1L,
                4,
                "수정된 내용",
                List.of("a.png"),
                List.of("remove.png")
        );

        mockMvc.perform(put("/member/review")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewService).updateReview(any(ReviewUpdateDto.class));
    }

    @Test
    @DisplayName("AI 요약 리뷰 조회 성공")
    void getAiReview_success() throws Exception {

        Book mockBook = Book.builder()
                .aiReview("AI 요약 리뷰입니다.")
                .build();

        given(aiReviewService.getBookById(10L)).willReturn(mockBook);

        mockMvc.perform(get("/review/book/10/ai"))
                .andExpect(status().isOk())
                .andExpect(content().string("AI 요약 리뷰입니다."));
    }
}


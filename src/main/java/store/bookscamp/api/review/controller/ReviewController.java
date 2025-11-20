package store.bookscamp.api.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.review.controller.request.ReviewCreateRequest;
import store.bookscamp.api.review.controller.request.ReviewUpdateRequest;
import store.bookscamp.api.review.service.ReviewService;
import store.bookscamp.api.review.service.dto.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰 API", description = "Review API입니다")
public class ReviewController {

    private final ReviewService reviewService;

    // todo: 도서 상세페이지 리뷰 조회 평균 평점

    @GetMapping("/member/review/reviewable")
    @Operation(summary = "read reviewable orderItems", description = "작성 가능한 리뷰 상품 조회 API")
    @RequiredRole("USER")
    public ResponseEntity<List<ReviewableItemDto>> getReviewableItems(HttpServletRequest request) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        return ResponseEntity.ok(reviewService.getReviewableItems(memberId));
    }

    @GetMapping("/member/review/my")
    @Operation(summary = "read my reviews", description = "유저가 작성한 리뷰 조회 API")
    @RequiredRole("USER")
    public ResponseEntity<List<MyReviewDto>> getMyReviews(HttpServletRequest request) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        return ResponseEntity.ok(reviewService.getMyReviews(memberId));
    }

    @GetMapping("/member/review/{reviewId}")
    @Operation(summary = "read update page", description = "리뷰 수정 페이지 API")
    @RequiredRole("USER")
    public ResponseEntity<MyReviewDto> getUpdateReview(
            @PathVariable Long reviewId,
            HttpServletRequest request
    ) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        return ResponseEntity.ok(reviewService.getUpdateReview(reviewId, memberId));
    }

    @GetMapping("/review/book/{bookId}")
    @Operation(summary = "read book review", description = "도서 리뷰 리스트 API")
    public ResponseEntity<Page<BookReviewDto>> getBookReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(
                reviewService.getBookReviews(bookId, pageable)
        );
    }

    @GetMapping("/review/book/{bookId}/avg")
    public ResponseEntity<Double> getBookAvgScore(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewAverageScore(bookId));
    }

    @PostMapping("/member/review")
    @Operation(summary = "create review", description = "리뷰 등록 API")
    @RequiredRole("USER")
    public ResponseEntity<Void> createReview(
            @RequestBody ReviewCreateRequest createReq,
            HttpServletRequest request
    ) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        reviewService.createReview(ReviewCreateDto.from(createReq, memberId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/member/review")
    @Operation(summary = "update review", description = "리뷰 수정 API")
    @RequiredRole("USER")
    public ResponseEntity<Void> updateReview(
            @RequestBody ReviewUpdateRequest updateReq,
            HttpServletRequest request
    ) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        reviewService.updateReview(ReviewUpdateDto.from(updateReq, memberId));
        return ResponseEntity.ok().build();
    }
}

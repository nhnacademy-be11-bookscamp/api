package store.bookscamp.api.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/review")
@Tag(name = "리뷰 API", description = "Review API입니다")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성 가능한 상품 조회
    @GetMapping("/reviewable")
    @Operation(summary = "read reviewable orderItems", description = "리뷰 작성 가능한 상품 조회 API")
    @RequiredRole("USER")
    public ResponseEntity<List<ReviewableItemDto>> getReviewableItems(HttpServletRequest request) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        return ResponseEntity.ok(reviewService.getReviewableItems(memberId));
    }

    // 작성한 리뷰 조회
    @GetMapping("/my")
    @Operation(summary = "read my reviews", description = "유저가 작성한 리뷰 조회 API")
    @RequiredRole("USER")
    public ResponseEntity<List<MyReviewDto>> getMyReviews(HttpServletRequest request) {
        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));
        return ResponseEntity.ok(reviewService.getMyReviews(memberId));
    }

    @PostMapping
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

    @PutMapping
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

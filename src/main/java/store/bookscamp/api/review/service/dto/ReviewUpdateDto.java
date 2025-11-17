package store.bookscamp.api.review.service.dto;

import store.bookscamp.api.review.controller.request.ReviewUpdateRequest;

import java.util.List;

public record ReviewUpdateDto(

        Long reviewId,
        Long memberId,
        Integer score,
        String content,
        List<String> imageUrls,
        List<String> removedImageUrls
) {
    public static ReviewUpdateDto from(ReviewUpdateRequest req, Long memberId) {
        return new ReviewUpdateDto(
                req.reviewId(),
                memberId,
                req.score(),
                req.content(),
                req.imageUrls(),
                req.removedImageUrls()
        );
    }
}

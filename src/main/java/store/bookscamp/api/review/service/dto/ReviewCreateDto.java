package store.bookscamp.api.review.service.dto;

import store.bookscamp.api.review.controller.request.ReviewCreateRequest;

import java.util.List;

public record ReviewCreateDto(
        Long orderItemId,
        Long memberId,
        Integer score,
        String content,
        List<String> imageUrls
) {
    public static ReviewCreateDto from(ReviewCreateRequest req, Long memberId) {
        return new ReviewCreateDto(
                req.orderItemId(),
                memberId,
                req.score(),
                req.content(),
                req.imageUrls()
        );
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }
}

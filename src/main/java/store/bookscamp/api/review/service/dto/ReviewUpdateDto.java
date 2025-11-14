package store.bookscamp.api.review.service.dto;

import store.bookscamp.api.review.controller.request.ReviewUpdateRequest;

import java.util.List;

public record ReviewUpdateDto(
        String content,
        Integer score,
        List<String> ImageUrls,
        List<String> removedImageUrls
) {
    public static ReviewUpdateDto from(ReviewUpdateRequest req) {
        return new ReviewUpdateDto(
                req.content(),
                req.score(),
                req.ImageUrls(),
                req.removedImageUrls()
        );
    }
}

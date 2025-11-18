package store.bookscamp.api.review.controller.request;

import java.util.List;

public record ReviewUpdateRequest(

        Long reviewId,
        Integer score,
        String content,
        List<String> imageUrls,
        List<String> removedImageUrls
) {}

package store.bookscamp.api.review.controller.request;

import java.util.List;

public record ReviewUpdateRequest(

        String content,
        Integer score,
        List<String> ImageUrls,
        List<String> removedImageUrls
) {}

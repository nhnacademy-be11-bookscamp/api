package store.bookscamp.api.review.controller.request;

import java.util.List;

public record ReviewCreateRequest(

        Long orderItemId,
        Integer score,
        String content,
        List<String> imageUrls
) {}

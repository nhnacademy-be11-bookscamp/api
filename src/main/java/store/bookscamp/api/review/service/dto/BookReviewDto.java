package store.bookscamp.api.review.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookReviewDto (

        Long reviewId,
        String username,
        String content,
        Integer score,
        LocalDateTime createdAt,
        List<String> imageUrls
) {}

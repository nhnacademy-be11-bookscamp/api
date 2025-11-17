package store.bookscamp.api.review.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MyReviewDto(
        Long reviewId,
        Long bookId,
        String bookTitle,
        String thumbnailUrl,
        String content,
        Integer score,
        LocalDateTime createdAt,
        List<String> imageUrls
) {}
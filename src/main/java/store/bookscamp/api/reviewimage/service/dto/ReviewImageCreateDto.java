package store.bookscamp.api.reviewimage.service.dto;

import store.bookscamp.api.review.entity.Review;

import java.util.List;

public record ReviewImageCreateDto (
        Review review,
        List<String> imageUrls
) {}

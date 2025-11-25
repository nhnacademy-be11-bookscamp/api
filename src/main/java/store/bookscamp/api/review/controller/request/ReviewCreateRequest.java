package store.bookscamp.api.review.controller.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReviewCreateRequest(

        Long orderItemId,

        @NotNull(message = "score는 필수입니다.")
        Integer score,

        String content,
        List<String> imageUrls
) {}

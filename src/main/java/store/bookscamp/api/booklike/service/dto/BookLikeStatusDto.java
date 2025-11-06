package store.bookscamp.api.booklike.service.dto;

import store.bookscamp.api.booklike.controller.response.BookLikeStatusResponse;

public record BookLikeStatusDto(
        boolean liked
) {

    public static BookLikeStatusResponse toDto(BookLikeStatusDto dto){
        return new BookLikeStatusResponse(
                dto.liked
        );
    }
}

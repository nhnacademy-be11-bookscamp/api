package store.bookscamp.api.booklike.service.dto;

import store.bookscamp.api.booklike.controller.response.BookLikeCountResponse;

public record BookLikeCountDto(

        Long likeCount
) {

    public static BookLikeCountResponse toDto(BookLikeCountDto dto){
        return new BookLikeCountResponse(
                dto.likeCount
        );
    }
}

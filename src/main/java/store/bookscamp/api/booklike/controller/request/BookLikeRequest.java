package store.bookscamp.api.booklike.controller.request;

import store.bookscamp.api.booklike.entity.BookLike;

public record BookLikeRequest(

        boolean liked
) {
}

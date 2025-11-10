package store.bookscamp.api.book.service.dto;

import org.springframework.data.domain.Pageable;

public record BookSearchRequest(
        Long categoryId,
        String keyword,
        String sortType,
        Pageable pageable
) {}

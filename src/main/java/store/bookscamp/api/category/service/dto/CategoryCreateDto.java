package store.bookscamp.api.category.service.dto;

import store.bookscamp.api.category.entity.Category;

public record CategoryCreateDto(

        Long id,
        Category parent,
        String name
) {
}

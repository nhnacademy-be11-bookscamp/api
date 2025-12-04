package store.bookscamp.api.category.controller.request;

import store.bookscamp.api.category.entity.Category;

public record CategoryUpdateRequest(
        Category parent,
        String name
) {
}

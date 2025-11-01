package store.bookscamp.api.category.controller.request;

import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.service.dto.CategoryUpdateDto;

public record CategoryUpdateRequest(

        Category parent,
        String name
) {

    public static CategoryUpdateRequest fromDto(CategoryUpdateDto dto){
        return new CategoryUpdateRequest(

                dto.parent(),
                dto.name()
        );
    }
}

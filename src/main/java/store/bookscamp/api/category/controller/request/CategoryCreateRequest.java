package store.bookscamp.api.category.controller.request;

import store.bookscamp.api.category.service.dto.CategoryCreateDto;

public record CategoryCreateRequest(

        Long parentId,
        String name
) {

    public CategoryCreateDto toDto(){
        return new CategoryCreateDto(
                parentId,
                name
        );
    }
}

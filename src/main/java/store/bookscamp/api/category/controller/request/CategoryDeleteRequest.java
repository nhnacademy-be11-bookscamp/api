package store.bookscamp.api.category.controller.request;

import store.bookscamp.api.category.service.dto.CategoryDeleteDto;

public record CategoryDeleteRequest(

        Long id
) {
    public CategoryDeleteDto toDto(){
        return new CategoryDeleteDto(
                id
        );
    }
}

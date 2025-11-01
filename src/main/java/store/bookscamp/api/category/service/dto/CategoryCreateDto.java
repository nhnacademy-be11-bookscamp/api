package store.bookscamp.api.category.service.dto;

import store.bookscamp.api.category.entity.Category;

public record CategoryCreateDto(

        Long parentId,
        String name
) {

    public CategoryCreateDto fromEntity(Category category){
        return new CategoryCreateDto(
                category.getParent().getId(),
                category.getName()
        );
    }
}

package store.bookscamp.api.category.service.dto;

import store.bookscamp.api.category.entity.Category;

public record CategoryUpdateDto(

        Long id,
        Category parent,
        String name
) {

    public CategoryUpdateDto fromEntity(Category category){
        return new CategoryUpdateDto(
                category.getId(),
                category.getParent(),
                category.getName()
        );
    }

    public Category toEntity(CategoryUpdateDto createDto){
        return new Category(
                createDto.parent,
                createDto.name
        );
    }
}

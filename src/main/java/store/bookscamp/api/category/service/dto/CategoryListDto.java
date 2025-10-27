package store.bookscamp.api.category.service.dto;

import store.bookscamp.api.category.entity.Category;

public record CategoryListDto(

        Long id,
        String name
) {

    public static CategoryListDto fromEntity(Category category){
        return new CategoryListDto(
                category.getId(),
                category.getName()
        );
    }
}

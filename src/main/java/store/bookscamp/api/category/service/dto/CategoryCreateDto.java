package store.bookscamp.api.category.service.dto;

public record CategoryCreateDto(
        Long parentId,
        String name
) {
}

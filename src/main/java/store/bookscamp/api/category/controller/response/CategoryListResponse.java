package store.bookscamp.api.category.controller.response;

import java.util.List;
import store.bookscamp.api.category.service.dto.CategoryListDto;

public record CategoryListResponse(
        Long id,
        String name,
        List<CategoryListResponse> children
) {

    public static CategoryListResponse from(CategoryListDto dto) {
        return new CategoryListResponse(
                dto.id(),
                dto.name(),
                dto.children().stream()
                        .map(CategoryListResponse::from)
                        .toList()
        );
    }

    public static List<CategoryListResponse> fromList(List<CategoryListDto> dtos) {
        return dtos.stream()
                .map(CategoryListResponse::from)
                .toList();
    }
}
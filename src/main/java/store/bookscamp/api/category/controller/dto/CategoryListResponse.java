package store.bookscamp.api.category.controller.dto;

import java.util.List;
import java.util.stream.Collectors;
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
                        .collect(Collectors.toList())
        );
    }

    public static List<CategoryListResponse> fromList(List<CategoryListDto> dtos) {
        return dtos.stream()
                .map(CategoryListResponse::from)
                .collect(Collectors.toList());
    }
}
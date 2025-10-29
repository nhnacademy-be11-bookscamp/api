package store.bookscamp.api.category.service.dto;

import java.util.ArrayList;
import java.util.List;

public record CategoryListDto(
        Long id,
        String name,
        List<CategoryListDto> children
) {
    public CategoryListDto(Long id, String name) {
        this(id, name, new ArrayList<>());
    }
}

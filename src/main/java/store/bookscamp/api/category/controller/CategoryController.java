package store.bookscamp.api.category.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.category.controller.dto.CategoryListResponse;
import store.bookscamp.api.category.service.CategoryService;
import store.bookscamp.api.category.service.dto.CategoryListDto;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryListResponse>> getCategoryTree() {

        List<CategoryListDto> categoryDtoTree = categoryService.getCategoryTree();

        List<CategoryListResponse> categoryResponseTree = categoryDtoTree.stream()
                .map(this::convertDtoToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryResponseTree);
    }

    private CategoryListResponse convertDtoToResponse(CategoryListDto dto) {
        List<CategoryListResponse> children = dto.children().stream()
                .map(this::convertDtoToResponse)
                .collect(Collectors.toList());

        return new CategoryListResponse(
                dto.id(),
                dto.name(),
                children
        );
    }
}
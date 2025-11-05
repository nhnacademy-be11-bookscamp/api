package store.bookscamp.api.category.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.category.controller.request.CategoryCreateRequest;
import store.bookscamp.api.category.controller.request.CategoryDeleteRequest;
import store.bookscamp.api.category.controller.request.CategoryUpdateRequest;
import store.bookscamp.api.category.controller.response.CategoryListResponse;
import store.bookscamp.api.category.service.CategoryService;
import store.bookscamp.api.category.service.dto.CategoryCreateDto;
import store.bookscamp.api.category.service.dto.CategoryDeleteDto;
import store.bookscamp.api.category.service.dto.CategoryListDto;
import store.bookscamp.api.category.service.dto.CategoryUpdateDto;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryListResponse>> getCategoryTree() {

        List<CategoryListDto> categoryDtoTree = categoryService.getCategoryTree();

        List<CategoryListResponse> categoryResponseTree = categoryDtoTree.stream()
                .map(this::convertDtoToResponse)
                .toList();

        return ResponseEntity.ok(categoryResponseTree);
    }

    @PostMapping("/admin/category/create")
    public ResponseEntity<Void> createCategory(@RequestBody CategoryCreateRequest request){

        CategoryCreateDto dto = request.toDto();
        categoryService.createCategory(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/admin/category/update/{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {

        CategoryUpdateDto dto = new CategoryUpdateDto(id, request.parent(), request.name());
        categoryService.updateCategory(dto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/category/delete/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id){

        CategoryDeleteRequest request = new CategoryDeleteRequest(id);
        CategoryDeleteDto dto = request.toDto();
        categoryService.deleteCategory(dto);

        return ResponseEntity.ok().build();
    }


    private CategoryListResponse convertDtoToResponse(CategoryListDto dto) {
        List<CategoryListResponse> children = dto.children().stream()
                .map(this::convertDtoToResponse)
                .toList();

        return new CategoryListResponse(
                dto.id(),
                dto.name(),
                children
        );
    }
}
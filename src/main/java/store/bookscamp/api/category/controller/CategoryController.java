package store.bookscamp.api.category.controller;

import java.util.ArrayList;
import java.util.List;
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
    public ResponseEntity<List<CategoryListResponse>> getAllCategories(){

        List<CategoryListDto> allCategories = categoryService.getAllCategories();
        List<CategoryListResponse> categoryListResponses = new ArrayList<>();

        for(CategoryListDto categoryListDto : allCategories){
            Long id = categoryListDto.id();
            String name = categoryListDto.name();

            categoryListResponses.add(new CategoryListResponse(id, name));
        }

        return ResponseEntity.ok(categoryListResponses);
    }
}

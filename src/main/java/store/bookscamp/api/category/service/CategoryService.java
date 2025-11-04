package store.bookscamp.api.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.dto.CategoryCreateDto;
import store.bookscamp.api.category.service.dto.CategoryDeleteDto;
import store.bookscamp.api.category.service.dto.CategoryListDto;
import store.bookscamp.api.category.service.dto.CategoryUpdateDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryListDto> getCategoryTree() {

        List<Category> allCategories = categoryRepository.findAll();

        Map<Long, CategoryListDto> dtoMap = allCategories.stream()
                .map(category -> new CategoryListDto(category.getId(), category.getName()))
                .collect(Collectors.toMap(CategoryListDto::id, dto -> dto));

        List<CategoryListDto> rootCategories = new ArrayList<>();

        for (Category category : allCategories) {

            CategoryListDto currentDto = dtoMap.get(category.getId());

            if (category.getParent() == null) {
                rootCategories.add(currentDto);
            } else {

                CategoryListDto parentDto = dtoMap.get(category.getParent().getId());

                if (parentDto != null) {

                    parentDto.children().add(currentDto);
                }
            }
        }

        return rootCategories;
    }

    @Transactional
    public void createCategory(CategoryCreateDto dto) {

        Category parentCategory = null;

        if (dto.parentId() != null) {
            parentCategory = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 부모 카테고리 ID입니다: " + dto.parentId()));
        }

        Category newCategory = new Category(parentCategory, dto.name());

        categoryRepository.save(newCategory);
    }

    @Transactional
    public void updateCategory(CategoryUpdateDto dto) {

        Category category = categoryRepository.findById(dto.id()).orElseThrow();
        category.updateName(dto.name());

    }

    @Transactional
    public void deleteCategory(CategoryDeleteDto dto){
        categoryRepository.deleteById(dto.id());
    }

    public CategoryListDto getCategory(Long id) {

        Category category = categoryRepository.findById(id).get();

        return new CategoryListDto(category.getId(), category.getName(), new ArrayList<>());
    }
}
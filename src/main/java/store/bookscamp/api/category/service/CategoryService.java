package store.bookscamp.api.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.dto.CategoryCreateDto;
import store.bookscamp.api.category.service.dto.CategoryDeleteDto;
import store.bookscamp.api.category.service.dto.CategoryListDto;
import store.bookscamp.api.category.service.dto.CategoryUpdateDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable("categories")
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
    @CacheEvict(value = "categories", allEntries = true)
    public void createCategory(CategoryCreateDto dto) {

        Category parentCategory = null;

        if (dto.parentId() != null) {
            parentCategory = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_PARENT_CATEGORY_ID));
        }

        if (categoryRepository.existsByNameAndParent(dto.name(), parentCategory)) {
            throw new ApplicationException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        Category newCategory = new Category(parentCategory, dto.name());

        categoryRepository.save(newCategory);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void updateCategory(CategoryUpdateDto dto) {

        Category category = categoryRepository.findById(dto.id())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));

        String newName = dto.name();
        String oldName = category.getName();
        Category parent = category.getParent();

        if (!oldName.equals(newName) && categoryRepository.existsByNameAndParent(newName, parent)) {
                throw new ApplicationException(ErrorCode.CATEGORY_NAME_DUPLICATE);
            }

        category.updateName(newName);

    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(CategoryDeleteDto dto){
        if (!categoryRepository.existsById(dto.id())) {
            throw new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        try {
            categoryRepository.deleteById(dto.id());
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(ErrorCode.CATEGORY_IN_USE);
        }
    }
}
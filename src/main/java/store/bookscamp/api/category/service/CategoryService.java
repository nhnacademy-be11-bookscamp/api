package store.bookscamp.api.category.service;

import java.util.ArrayList;
import java.util.HashMap;
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
    private final CategoryQueryService categoryQueryService;

    @Cacheable("categories")
    public List<CategoryListDto> getCategoryTree() {
        return categoryQueryService.getCategoryTree();
    }

    @Cacheable("categoryChildMap")
    public Map<Long, List<Long>> getCategoryChildMap() {
        return categoryQueryService.getCategoryChildMap();
    }

    @Transactional
    @CacheEvict(value = {"categories", "categoryChildMap"}, allEntries = true)
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
    @CacheEvict(value = {"categories", "categoryChildMap"}, allEntries = true)
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
    @CacheEvict(value = {"categories", "categoryChildMap"}, allEntries = true)
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

    public List<Long> getDescendantIdsIncludingSelf(Long categoryId) {

        // 1. 캐시된 <부모, 자식 ID 목록> 맵을 가져옵니다. (매우 빠름)
        Map<Long, List<Long>> childMap = getCategoryChildMap();

        List<Long> descendantIds = new ArrayList<>();

        // 2. 재귀 헬퍼 메서드 호출
        collectDescendantIds(categoryId, descendantIds, childMap);

        return descendantIds;
    }

    private void collectDescendantIds(Long currentId, List<Long> ids, Map<Long, List<Long>> childMap) {

        // 1. 자기 자신을 리스트에 추가
        ids.add(currentId);

        // 2. 캐시된 맵에서 현재 ID의 직계 자식 목록을 조회
        List<Long> directChildren = childMap.get(currentId);

        // 3. 자식들이 있으면, 각 자식에 대해 재귀 호출
        if (directChildren != null) {
            for (Long childId : directChildren) {
                collectDescendantIds(childId, ids, childMap);
            }
        }
    }
}
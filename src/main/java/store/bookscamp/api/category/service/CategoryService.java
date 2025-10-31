package store.bookscamp.api.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.dto.CategoryListDto;

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
}
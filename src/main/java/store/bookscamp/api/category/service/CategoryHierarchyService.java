package store.bookscamp.api.category.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class CategoryHierarchyService {

    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryRepository categoryRepository;

    public List<Long> findAllCategoryIdsIncludingParents(List<Long> bookIds) {
        List<Long> directCategoryIds = bookIds.stream()
                .flatMap(bookId -> bookCategoryRepository.findByBook_Id(bookId).stream())
                .map(bookCategory -> bookCategory.getCategory().getId())
                .distinct()
                .toList();

        return directCategoryIds.stream()
                .flatMap(categoryId -> getAllParentCategoryIds(categoryId).stream())
                .distinct()
                .toList();
    }

    public List<Long> getAllParentCategoryIds(Long categoryId) {
        List<Long> parentCategoryIds = new ArrayList<>();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));

        while (category != null) {
            parentCategoryIds.add(category.getId());
            category = category.getParent();
        }

        return parentCategoryIds;
    }
}


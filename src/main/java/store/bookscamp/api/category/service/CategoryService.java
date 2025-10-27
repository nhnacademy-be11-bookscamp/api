package store.bookscamp.api.category.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.dto.CategoryListDto;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryListDto> getAllCategories(){

        List<Category> categoryList = categoryRepository.findAll();
        List<CategoryListDto> categoryListDtoList = new ArrayList<>();

        for(Category category : categoryList){
            Long id = category.getId();
            String name = category.getName();

            categoryListDtoList.add(new CategoryListDto(id, name));
        }

        return categoryListDtoList;
    }
}

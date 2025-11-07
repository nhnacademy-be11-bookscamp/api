package store.bookscamp.api.category.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category getCategoryById(@Param("categoryId") Long categoryId);

    boolean existsByNameAndParent(String name, Category parent);
}

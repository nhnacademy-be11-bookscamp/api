package store.bookscamp.api.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

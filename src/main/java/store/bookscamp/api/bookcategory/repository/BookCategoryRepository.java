package store.bookscamp.api.bookcategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.bookcategory.entity.BookCategory;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
}

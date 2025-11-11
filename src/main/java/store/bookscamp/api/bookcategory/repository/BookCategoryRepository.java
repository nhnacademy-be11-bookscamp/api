package store.bookscamp.api.bookcategory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.bookcategory.entity.BookCategory;
import store.bookscamp.api.category.entity.Category;

import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {

    Optional<BookCategory> findByBook(Book book);

    void deleteByBook(Book book);

    boolean existsByBookAndCategory(Book book, Category category);

    List<BookCategory> findByBook_Id(Long bookId);
}

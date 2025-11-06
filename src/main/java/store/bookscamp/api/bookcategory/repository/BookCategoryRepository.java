package store.bookscamp.api.bookcategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.bookcategory.entity.BookCategory;

import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {

    Optional<BookCategory> findByBook(Book book);

    void deleteByBook(Book book);

}

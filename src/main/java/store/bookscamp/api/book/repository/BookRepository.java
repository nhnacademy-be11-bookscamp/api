package store.bookscamp.api.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    Book getBookById(Long id);
}

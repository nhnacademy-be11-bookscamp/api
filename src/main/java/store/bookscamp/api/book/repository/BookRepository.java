package store.bookscamp.api.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

}

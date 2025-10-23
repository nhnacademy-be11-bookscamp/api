package store.bookcamp.api.page.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;

public interface PageRepository extends JpaRepository<Book, Long> {

//    Page<Book> findAll(String title, Pageable pageable);
    Page<Book> findAllByTitleContaining(String title, Pageable pageable);


}

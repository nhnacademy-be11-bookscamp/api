package store.bookscamp.api.book.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store.bookscamp.api.book.entity.Book;

@Repository
public interface BookRepositoryCustom {

    Page<Book> getBooks(Long categoryId, String keyword, String sortType, Pageable pageable);
}

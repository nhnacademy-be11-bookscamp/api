package store.bookscamp.api.book.repository.custom;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store.bookscamp.api.book.entity.Book;

@Repository
public interface BookRepositoryCustom {

    Page<Book> getBooks(String keyword, Pageable pageable);

    List<Book> getRecommendBooks();

    Page<Book> getNewBooks(Pageable pageable);

    Page<Book> getBestSellers(Pageable pageable);
}

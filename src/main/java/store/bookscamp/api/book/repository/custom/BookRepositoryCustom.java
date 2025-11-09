package store.bookscamp.api.book.repository.custom;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store.bookscamp.api.book.entity.Book;

@Repository
public interface BookRepositoryCustom {

    Page<Book> getBooks(List<Long> categoryIds, String sortType, Pageable pageable);
}

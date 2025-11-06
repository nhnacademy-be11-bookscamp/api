package store.bookscamp.api.booktag.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.booktag.entity.BookTag;
import store.bookscamp.api.tag.entity.Tag;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {

    List<BookTag> findAllByBook(Book book);

    void deleteByBook(Book book);

}

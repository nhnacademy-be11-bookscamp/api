package store.bookscamp.api.booktag.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.booktag.entity.BookTag;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {

    @Query("SELECT bt FROM BookTag bt WHERE bt.book.id = :bookId")
    List<BookTag> findAllByBookId(@Param("bookId") Long id);

    void deleteByBook(Book book);

    List<BookTag> findByBook_Id(Long bookId);
}

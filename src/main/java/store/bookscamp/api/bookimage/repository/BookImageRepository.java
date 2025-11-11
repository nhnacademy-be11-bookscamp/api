package store.bookscamp.api.bookimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.bookimage.entity.BookImage;

import java.util.List;
import java.util.Optional;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {

    List<BookImage> findAllByBook(Book book);

    Optional<BookImage> findByImageUrl(String imageUrl);

    List<BookImage> findByBook(Book book);

    List<BookImage> findByBook_Id(Long bookId);
}

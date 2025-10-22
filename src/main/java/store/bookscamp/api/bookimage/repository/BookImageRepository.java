package store.bookscamp.api.bookimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.bookimage.entity.BookImage;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
}

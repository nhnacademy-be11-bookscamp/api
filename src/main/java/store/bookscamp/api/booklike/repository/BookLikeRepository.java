package store.bookscamp.api.booklike.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.booklike.entity.BookLike;

public interface BookLikeRepository extends JpaRepository<BookLike, Long> {
}

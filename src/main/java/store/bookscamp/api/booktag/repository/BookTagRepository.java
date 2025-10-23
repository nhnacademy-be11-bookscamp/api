package store.bookscamp.api.booktag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.booktag.entity.BookTag;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {
}

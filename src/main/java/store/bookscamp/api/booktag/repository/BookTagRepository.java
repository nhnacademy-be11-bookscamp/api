package store.bookscamp.api.booktag.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.booktag.entity.BookTag;
import store.bookscamp.api.tag.entity.Tag;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {
}

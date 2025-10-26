package store.bookscamp.api.tag.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.tag.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}

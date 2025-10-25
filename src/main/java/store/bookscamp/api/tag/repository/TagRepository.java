package store.bookscamp.api.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.tag.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
}

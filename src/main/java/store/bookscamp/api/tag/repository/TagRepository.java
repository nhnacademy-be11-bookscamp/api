package store.bookscamp.api.tag.repository;

import feign.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.tag.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
    Optional<Tag> findByName(String name);
    Tag getTagById(@Param("tagId") Long tagId);
}
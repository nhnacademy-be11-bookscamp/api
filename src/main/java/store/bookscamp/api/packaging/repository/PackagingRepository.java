package store.bookscamp.api.packaging.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.packaging.entity.Packaging;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {
    boolean existsByName(String name);
    Optional<Packaging> findByName(String name);

}

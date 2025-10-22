package store.bookscamp.api.packaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.packaging.entity.Packaging;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {
}

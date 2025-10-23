package store.bookscamp.api.contributor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.contributor.entity.Contributor;

public interface ContributorRepository extends JpaRepository<Contributor, Long> {
    
}

package store.bookcamp.api.contributor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookcamp.api.contributor.entity.Contributor;

public interface ContributorRepository extends JpaRepository<Contributor, Long> {
    
}

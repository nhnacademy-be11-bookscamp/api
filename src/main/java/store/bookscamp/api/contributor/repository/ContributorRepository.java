package store.bookscamp.api.contributor.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.contributor.entity.Contributor;

public interface ContributorRepository extends JpaRepository<Contributor, Long> {
    Optional<Contributor> findByContributors(String contributors);
    boolean existsByContributors(String contributors);
}
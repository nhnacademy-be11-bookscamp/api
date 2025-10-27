package store.bookscamp.api.rank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.rank.entity.Rank;

public interface RankRepository extends JpaRepository<Rank, Long> {
}

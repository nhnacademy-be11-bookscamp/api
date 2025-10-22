package store.bookscamp.api.pointhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}

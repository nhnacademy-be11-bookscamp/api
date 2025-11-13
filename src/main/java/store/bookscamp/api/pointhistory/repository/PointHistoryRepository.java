package store.bookscamp.api.pointhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findAllHistoryByMemberId(Long memberId);
}

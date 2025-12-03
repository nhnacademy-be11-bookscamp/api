package store.bookscamp.api.pointhistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Page<PointHistory> findAllHistoryByMemberId(Long memberId, Pageable pageable);

    List<PointHistory> findByOrderInfo_Id(Long orderInfoId);
}

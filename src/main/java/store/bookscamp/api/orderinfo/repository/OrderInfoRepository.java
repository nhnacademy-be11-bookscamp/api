package store.bookscamp.api.orderinfo.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.orderinfo.entity.OrderInfo;


public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {
    // 회원 주문 목록 조회 (페이징 + 정렬은 Pageable에서 처리)
    @Query("""
        SELECT oi
        FROM OrderInfo oi
        WHERE oi.member.id = :memberId
    """)
    Page<OrderInfo> findByMemberId(@Param("memberId") Long orderId, Pageable pageable);

    // 주문 번호로 조회
    Optional<OrderInfo> findByOrderNumber(String orderNumber);
}

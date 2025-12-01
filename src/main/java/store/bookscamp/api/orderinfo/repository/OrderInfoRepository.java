package store.bookscamp.api.orderinfo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;


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

    @Query("""
        SELECT oi
        FROM OrderInfo oi
        JOIN FETCH oi.delivery d
        WHERE oi.orderStatus = :status
        AND d.shippingDate < :shippingDateBefore
    """)
    List<OrderInfo> findShippingOrdersToComplete(
        @Param("status") OrderStatus status,
        @Param("shippingDateBefore") java.time.LocalDate shippingDateBefore
    );
}

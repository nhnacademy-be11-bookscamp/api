package store.bookscamp.api.orderitem.repository;

import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.orderitem.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 주문 한 건에 포함된 OrderItem 전체 조회
     */
    @Query("""
        SELECT oi
        FROM OrderItem oi
        WHERE oi.orderInfo.id = :orderId
    """)
    List<OrderItem> findByOrderInfoId(Long orderId);

    @Query(value = """
        SELECT b.title
        FROM order_item oi
        LEFT JOIN book b ON b.id = oi.book_id
        WHERE oi.order_id = :orderInfoId
        ORDER BY oi.id ASC
        LIMIT 1
    """, nativeQuery = true)
    String findRepresentativeBookTitleIncludingDeleted(@Param("orderInfoId") Long orderInfoId);

}

package store.bookscamp.api.orderitem.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 주문 한 건에 포함된 OrderItem 전체 조회
     */
    List<OrderItem> findByOrderInfoId(Long orderId);
}

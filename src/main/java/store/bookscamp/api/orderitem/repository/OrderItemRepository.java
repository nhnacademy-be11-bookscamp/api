package store.bookscamp.api.orderitem.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // TODO : 주문 내역 상세 조회
    List<OrderItem> findByOrderId(Long orderId);
}

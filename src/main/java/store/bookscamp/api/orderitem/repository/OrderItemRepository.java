package store.bookscamp.api.orderitem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}

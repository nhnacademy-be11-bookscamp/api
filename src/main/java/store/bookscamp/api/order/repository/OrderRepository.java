package store.bookscamp.api.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

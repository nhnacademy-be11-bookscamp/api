package store.bookscamp.api.orderinfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;

public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {
}

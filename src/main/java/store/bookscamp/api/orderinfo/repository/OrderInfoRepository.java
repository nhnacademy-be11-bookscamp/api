package store.bookscamp.api.orderinfo.repository;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.entity.OrderStatus;

public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {

}

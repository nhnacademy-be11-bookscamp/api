package store.bookscamp.api.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.delivery.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}

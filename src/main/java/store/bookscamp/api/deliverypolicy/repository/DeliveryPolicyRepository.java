package store.bookscamp.api.deliverypolicy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;

public interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {
}

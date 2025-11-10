package store.bookscamp.api.deliverypolicy.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;

public interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {
    // 최신/유일 정책 1건을 가져온다는 전제
    @Query("select p from DeliveryPolicy p order by p.id desc limit 1")
    Optional<DeliveryPolicy> findCurrent();
}

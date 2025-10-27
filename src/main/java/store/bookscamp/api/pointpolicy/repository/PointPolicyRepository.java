package store.bookscamp.api.pointpolicy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
}

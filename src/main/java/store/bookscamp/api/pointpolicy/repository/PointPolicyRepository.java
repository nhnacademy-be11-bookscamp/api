package store.bookscamp.api.pointpolicy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.TargetType;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;

import java.util.Optional;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {

    Optional<PointPolicy> findByPointPolicyType(PointPolicyType pointPolicyType);
}

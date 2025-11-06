package store.bookscamp.api.coupon.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.TargetType;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByTargetType(TargetType targetType);
}

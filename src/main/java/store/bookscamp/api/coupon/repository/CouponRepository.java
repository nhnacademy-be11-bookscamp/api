package store.bookscamp.api.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}

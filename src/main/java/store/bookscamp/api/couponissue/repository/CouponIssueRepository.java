package store.bookscamp.api.couponissue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.couponissue.entity.CouponIssue;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
}

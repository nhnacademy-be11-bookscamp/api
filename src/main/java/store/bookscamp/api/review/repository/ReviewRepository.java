package store.bookscamp.api.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}

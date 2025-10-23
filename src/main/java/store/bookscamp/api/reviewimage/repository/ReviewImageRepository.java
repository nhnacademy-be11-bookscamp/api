package store.bookscamp.api.reviewimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.reviewimage.entity.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}

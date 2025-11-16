package store.bookscamp.api.reviewimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.reviewimage.entity.ReviewImage;

import java.util.Optional;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    Optional<ReviewImage> findByImageUrl(String imageUrl);
}

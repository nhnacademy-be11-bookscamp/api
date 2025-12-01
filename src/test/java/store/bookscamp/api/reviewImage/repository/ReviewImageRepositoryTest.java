package store.bookscamp.api.reviewImage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.reviewimage.entity.ReviewImage;
import store.bookscamp.api.reviewimage.repository.ReviewImageRepository;

@SpringBootTest
@Transactional
class ReviewImageRepositoryTest {

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private EntityManager em;

    private Review createReview() {
        Review r = new Review(null, null, "content", 5);
        em.persist(r);
        return r;
    }

    @Test
    @DisplayName("이미지 저장 및 단건 조회 성공")
    void findByImageUrl_success() {
        Review review = createReview();
        ReviewImage img = new ReviewImage(review, "url1");
        reviewImageRepository.save(img);

        var result = reviewImageRepository.findByImageUrl("url1");
        assertThat(result).isPresent();
        assertThat(result.get().getImageUrl()).isEqualTo("url1");
    }

    @Test
    @DisplayName("없는 이미지 URL 조회 시 empty 반환")
    void findByImageUrl_empty() {
        var result = reviewImageRepository.findByImageUrl("none");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("리뷰 ID 기준 조회 성공")
    void findByReviewId_success() {
        Review review = createReview();

        ReviewImage img1 = new ReviewImage(review, "url1");
        ReviewImage img2 = new ReviewImage(review, "url2");

        reviewImageRepository.save(img1);
        reviewImageRepository.save(img2);

        List<ReviewImage> result = reviewImageRepository.findByReviewId(review.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting("imageUrl").containsExactlyInAnyOrder("url1", "url2");
    }

    @Test
    @DisplayName("해당 리뷰에 이미지 없으면 empty 반환")
    void findByReviewId_empty() {
        Review review = createReview();
        List<ReviewImage> result = reviewImageRepository.findByReviewId(review.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이미지 삭제 후 조회 시 empty")
    void deleteImage_success() {
        Review review = createReview();
        ReviewImage img = new ReviewImage(review, "url1");
        reviewImageRepository.save(img);

        reviewImageRepository.delete(img);
        em.flush();
        em.clear();

        var result = reviewImageRepository.findByImageUrl("url1");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 이미지 저장 후 개별 삭제")
    void delete_specificImage_success() {
        Review review = createReview();

        ReviewImage img1 = new ReviewImage(review, "a");
        ReviewImage img2 = new ReviewImage(review, "b");
        ReviewImage img3 = new ReviewImage(review, "c");

        reviewImageRepository.save(img1);
        reviewImageRepository.save(img2);
        reviewImageRepository.save(img3);

        reviewImageRepository.delete(img2);
        em.flush();
        em.clear();

        var remain = reviewImageRepository.findByReviewId(review.getId());
        assertThat(remain).hasSize(2);
        assertThat(remain).extracting("imageUrl")
                .containsExactlyInAnyOrder("a", "c");
    }

    @Test
    @DisplayName("모든 이미지 삭제 가능")
    void deleteAll_success() {
        Review review = createReview();

        reviewImageRepository.save(new ReviewImage(review, "a"));
        reviewImageRepository.save(new ReviewImage(review, "b"));

        reviewImageRepository.deleteAll();
        em.flush();
        em.clear();

        List<ReviewImage> result = reviewImageRepository.findAll();
        assertThat(result).isEmpty();
    }
}


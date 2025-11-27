package store.bookscamp.api.reviewImage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.reviewimage.entity.ReviewImage;
import store.bookscamp.api.reviewimage.repository.ReviewImageRepository;
import store.bookscamp.api.reviewimage.service.ReviewImageService;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageCreateDto;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageDeleteDto;

@SpringBootTest
@Transactional
class ReviewImageServiceTest {

    @Autowired
    private ReviewImageService reviewImageService;

    @MockitoBean
    private ReviewImageRepository reviewImageRepository;

    private Review createReview() {
        return new Review(null, null, "content", 5);
    }

    @Test
    @DisplayName("리뷰 null → REVIEW_NOT_FOUND")
    void createReviewImage_nullReview_fail() {
        ReviewImageCreateDto dto = new ReviewImageCreateDto(null, List.of("a"));
        assertThatThrownBy(() -> reviewImageService.createReviewImage(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미지 리스트 null → 동작 없음")
    void createReviewImage_nullList_success() {
        Review review = createReview();
        ReviewImageCreateDto dto = new ReviewImageCreateDto(review, null);
        reviewImageService.createReviewImage(dto);
        verify(reviewImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미지 리스트 empty → 동작 없음")
    void createReviewImage_emptyList_success() {
        Review review = createReview();
        ReviewImageCreateDto dto = new ReviewImageCreateDto(review, List.of());
        reviewImageService.createReviewImage(dto);
        verify(reviewImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("리뷰 이미지 저장 성공")
    void createReviewImage_success() {
        Review review = createReview();
        ReviewImageCreateDto dto = new ReviewImageCreateDto(review, List.of("a", "b"));
        reviewImageService.createReviewImage(dto);
        verify(reviewImageRepository, times(2)).save(any(ReviewImage.class));
    }

    @Test
    @DisplayName("삭제 리스트 null → 동작 없음")
    void deleteReviewImage_nullList_success() {
        ReviewImageDeleteDto dto = new ReviewImageDeleteDto(null);
        reviewImageService.deleteReviewImage(dto);
        verify(reviewImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("삭제 리스트 empty → 동작 없음")
    void deleteReviewImage_emptyList_success() {
        ReviewImageDeleteDto dto = new ReviewImageDeleteDto(List.of());
        reviewImageService.deleteReviewImage(dto);
        verify(reviewImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("삭제 대상 없음 → IMAGE_NOT_FOUND")
    void deleteReviewImage_notFound_fail() {
        when(reviewImageRepository.findByImageUrl("x")).thenReturn(Optional.empty());
        ReviewImageDeleteDto dto = new ReviewImageDeleteDto(List.of("x"));
        assertThatThrownBy(() -> reviewImageService.deleteReviewImage(dto))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.IMAGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("리뷰 이미지 삭제 성공")
    void deleteReviewImage_success() {
        ReviewImage img = new ReviewImage(createReview(), "a");
        when(reviewImageRepository.findByImageUrl("a")).thenReturn(Optional.of(img));
        ReviewImageDeleteDto dto = new ReviewImageDeleteDto(List.of("a"));
        reviewImageService.deleteReviewImage(dto);
        verify(reviewImageRepository, times(1)).delete(img);
    }

    @Test
    @DisplayName("리뷰 이미지 조회 성공")
    void getReviewImages_success() {
        ReviewImage img1 = new ReviewImage(createReview(), "a");
        ReviewImage img2 = new ReviewImage(createReview(), "b");
        when(reviewImageRepository.findByReviewId(1L)).thenReturn(List.of(img1, img2));
        List<String> result = reviewImageService.getReviewImages(1L);
        assertThat(result).containsExactly("a", "b");
    }

    @Test
    @DisplayName("리뷰 이미지 없으면 empty")
    void getReviewImages_empty() {
        when(reviewImageRepository.findByReviewId(9L)).thenReturn(List.of());
        List<String> result = reviewImageService.getReviewImages(9L);
        assertThat(result).isEmpty();
    }
}

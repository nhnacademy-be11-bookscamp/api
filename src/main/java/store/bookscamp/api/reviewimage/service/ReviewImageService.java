package store.bookscamp.api.reviewimage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.reviewimage.entity.ReviewImage;
import store.bookscamp.api.reviewimage.repository.ReviewImageRepository;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageCreateDto;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageDeleteDto;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;

    @Transactional
    public void createReviewImage(ReviewImageCreateDto dto) {

        if (dto.review() == null) {
            throw new ApplicationException(ErrorCode.REVIEW_NOT_FOUND);
        }

        if (dto.imageUrls() == null || dto.imageUrls().isEmpty()) {
            return;
        }

        for (String url : dto.imageUrls()) {
            reviewImageRepository.save(new ReviewImage(dto.review(), url));
        }
    }

    @Transactional
    public void deleteReviewImage(ReviewImageDeleteDto dto) {

        if (dto.imageUrls() == null || dto.imageUrls().isEmpty()) {
            return;
        }

        for (String url : dto.imageUrls()) {
            ReviewImage image = reviewImageRepository.findByImageUrl(url)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.IMAGE_NOT_FOUND));
            reviewImageRepository.delete(image);
        }
    }
}

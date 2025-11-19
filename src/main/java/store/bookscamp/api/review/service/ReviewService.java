package store.bookscamp.api.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.service.PointHistoryService;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.entity.PointPolicyType;
import store.bookscamp.api.pointpolicy.repository.PointPolicyRepository;
import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.review.repository.ReviewRepository;
import store.bookscamp.api.review.repository.ReviewQueryRepository;
import store.bookscamp.api.review.service.dto.BookReviewDto;
import store.bookscamp.api.review.service.dto.MyReviewDto;
import store.bookscamp.api.review.service.dto.ReviewCreateDto;
import store.bookscamp.api.review.service.dto.ReviewUpdateDto;
import store.bookscamp.api.review.service.dto.ReviewableItemDto;
import store.bookscamp.api.reviewimage.entity.ReviewImage;
import store.bookscamp.api.reviewimage.service.ReviewImageService;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageCreateDto;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageDeleteDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewImageService reviewImageService;
    private final PointHistoryService pointHistoryService;
    private final BookImageService bookImageService;

    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    @Transactional
    public void createReview(ReviewCreateDto dto) {

        Member member = getMember(dto.memberId());
        OrderItem orderItem = getOrderItem(dto.orderItemId());

        if (reviewRepository.existsByOrderItemAndMember(orderItem, member)) {
            throw new ApplicationException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review(orderItem, member, dto.content(), dto.score());
        reviewRepository.save(review);

        reviewImageService.createReviewImage(new ReviewImageCreateDto(review, dto.imageUrls()));

        PointPolicyType policyType = dto.hasImages()
                ? PointPolicyType.REVIEW_IMAGE
                : PointPolicyType.REVIEW_TEXT;

        PointPolicy policy = pointPolicyRepository.findByPointPolicyType(policyType)
                .orElseThrow(() -> new ApplicationException(ErrorCode.POINT_POLICY_NOT_FOUND));

        pointHistoryService.earnPoint(
                new PointHistoryEarnDto(dto.memberId(), null, PointType.EARN, policy.getRewardValue()),
                dto.memberId()
        );
    }

    @Transactional
    public void updateReview(ReviewUpdateDto dto) {

        Review review = getReview(dto.reviewId());

        if (!review.getMember().getId().equals(dto.memberId())) {
            throw new ApplicationException(ErrorCode.NO_PERMISSION);
        }

        review.update(dto.content(), dto.score());

        reviewImageService.deleteReviewImage(new ReviewImageDeleteDto(dto.removedImageUrls()));
        reviewImageService.createReviewImage(new ReviewImageCreateDto(review, dto.imageUrls()));
    }

    public List<ReviewableItemDto> getReviewableItems(Long memberId) {
        return reviewQueryRepository.findReviewableItems(memberId);
    }

    public List<MyReviewDto> getMyReviews(Long memberId) {
        return reviewQueryRepository.findMyReviews(memberId);
    }

    public MyReviewDto getUpdateReview(Long reviewId, Long memberId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰인지 검증
        if (!review.getMember().getId().equals(memberId)) {
            throw new ApplicationException(ErrorCode.NO_PERMISSION);
        }

        // 리뷰 이미지 조회
        List<String> imageUrls = reviewImageService.getReviewImages(reviewId);

        // 도서 썸네일 조회
        Long bookId = review.getOrderItem().getBook().getId();
        String thumbnailUrl = bookImageService.getThumbnailUrl(bookId);

        return new MyReviewDto(
                review.getId(),
                bookId,
                review.getOrderItem().getBook().getTitle(),
                thumbnailUrl,
                review.getContent(),
                review.getScore(),
                review.getCreatedAt(),
                imageUrls
        );
    }

    public Page<BookReviewDto> getBookReviews(Long bookId, Pageable pageable) {

        Page<Review> page = reviewRepository.findByOrderItemBookId(bookId, pageable);

        List<BookReviewDto> content = page.getContent().stream()
                .map(r -> {
                    List<String> images = reviewImageService.getReviewImages(r.getId());

                    return new BookReviewDto(
                            r.getId(),
                            r.getMember().getUsername(),
                            r.getContent(),
                            r.getScore(),
                            r.getCreatedAt(),
                            images
                    );
                })
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Double getReviewAverageScore(Long bookId) {

        Double avg = reviewRepository.getAvgScore(bookId);
        if (avg == null) {
            return 0.0;
        }
        
        return Math.round(avg * 10) / 10.0;
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private OrderItem getOrderItem(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.REVIEW_NOT_FOUND));
    }
}

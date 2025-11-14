package store.bookscamp.api.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.service.PointHistoryService;
import store.bookscamp.api.pointhistory.service.dto.PointHistoryEarnDto;
import store.bookscamp.api.review.entity.Review;
import store.bookscamp.api.review.repository.ReviewRepository;
import store.bookscamp.api.review.service.dto.ReviewCreateDto;
import store.bookscamp.api.review.service.dto.ReviewUpdateDto;
import store.bookscamp.api.reviewimage.service.ReviewImageService;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageCreateDto;
import store.bookscamp.api.reviewimage.service.dto.ReviewImageDeleteDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageService reviewImageService;
    private final PointHistoryService pointHistoryService;

    @Transactional
    public void createReview(ReviewCreateDto dto) {

        Member member = getMember(dto.memberId());
        OrderItem orderItem = getOrderItem(dto.orderItemId());

        if (reviewRepository.existsByOrderItemAndMember(member, orderItem)) {
            throw new ApplicationException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review(orderItem, member, dto.content(), dto.score());
        reviewRepository.save(review);

        reviewImageService.createReviewImage(new ReviewImageCreateDto(review, dto.imageUrls()));

        int reward = dto.hasImages() ? 500 : 200;
        pointHistoryService.earnPoint(
                new PointHistoryEarnDto(dto.memberId(), null, PointType.EARN, reward),
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

package store.bookscamp.api.orderinfo.service;

import static store.bookscamp.api.common.exception.ErrorCode.*;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.DiscountType.RATE;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.PENDING;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.couponissue.query.CouponIssueSearchQuery;
import store.bookscamp.api.couponissue.query.dto.CouponSearchConditionDto;
import store.bookscamp.api.delivery.entity.Delivery;
import store.bookscamp.api.delivery.repository.deliveryRepository;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.nonmember.entity.NonMember;
import store.bookscamp.api.nonmember.repository.NonMemberRepository;
import store.bookscamp.api.orderinfo.service.dto.DeliveryInfoDto;
import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderRequestDto;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.repository.PointHistoryRepository;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCreateService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final deliveryRepository deliveryRepository;
    private final NonMemberRepository nonMemberRepository;

    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final PackagingRepository packagingRepository;
    private final MemberRepository memberRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueSearchQuery couponIssueSearchQuery;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public OrderCreateDto createOrder(OrderRequestDto request, Long memberId) {
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ApplicationException(DELIVERY_POLICY_NOT_FOUND));

        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
        } else {
            if (request.nonMemberInfo() == null) {
                throw new ApplicationException(NON_MEMBER_INFO_REQUIRED);
            }
        }

        int netAmount = 0;
        int packagingFee = 0;

        for (OrderItemCreateDto itemRequest : request.items()) {
            Book book = bookRepository.findById(itemRequest.bookId())
                    .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));

            validateBook(book, itemRequest.quantity());

            netAmount += book.getSalePrice() * itemRequest.quantity();

            if (itemRequest.packagingId() != null) {
                Packaging packaging = packagingRepository.findById(itemRequest.packagingId())
                        .orElseThrow(() -> new ApplicationException(PACKAGING_NOT_FOUND));
                packagingFee += packaging.getPrice();
            }
        }

        int deliveryFee = (netAmount >= deliveryPolicy.getFreeDeliveryThreshold())
                ? 0
                : deliveryPolicy.getBaseDeliveryFee();

        int totalAmount = netAmount + deliveryFee + packagingFee;

        int discountAmount = 0;
        CouponIssue couponIssue = null;
        if (request.couponIssueId() != null) {
            if (member == null) {
                throw new ApplicationException(COUPON_NOT_ALLOWED_FOR_NON_MEMBER);
            }
            
            couponIssue = validateAndGetCouponIssue(request.couponIssueId(), member, request.items(), netAmount);
            discountAmount = calculateCouponDiscount(couponIssue, netAmount);
        }

        int usedPoint = (request.usedPoint() != null) ? request.usedPoint() : 0;
        if (usedPoint > 0) {
            if (member == null) {
                throw new ApplicationException(POINT_NOT_ALLOWED_FOR_NON_MEMBER);
            }
            if (member.getPoint() < usedPoint) {
                throw new ApplicationException(INSUFFICIENT_POINT);
            }
        }

        int finalPaymentAmount = totalAmount - discountAmount - usedPoint;
        if (finalPaymentAmount < 0) {
            finalPaymentAmount = 0;
        }

        DeliveryInfoDto deliveryInfo = request.deliveryInfo();
        LocalDate shippingDate = calculateShippingDate(deliveryInfo.desiredDeliveryDate());

        Delivery delivery = new Delivery(
                deliveryPolicy,
                shippingDate,
                deliveryInfo.desiredDeliveryDate(),
                deliveryInfo.recipientName(),
                deliveryInfo.recipientPhone(),
                deliveryInfo.zipCode(),
                deliveryInfo.roadNameAddress(),
                deliveryInfo.detailAddress(),
                deliveryInfo.deliveryMemo()
        );
        deliveryRepository.save(delivery);

        OrderInfo orderInfo = new OrderInfo(
                member,
                couponIssue,
                delivery,
                netAmount,
                totalAmount,
                deliveryFee,
                packagingFee,
                discountAmount,
                finalPaymentAmount,
                PENDING,
                usedPoint
        );
        orderInfoRepository.save(orderInfo);

        for (OrderItemCreateDto itemRequest : request.items()) {
            Book book = bookRepository.findById(itemRequest.bookId())
                    .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));

            Packaging packaging = null;
            if (itemRequest.packagingId() != null) {
                packaging = packagingRepository.findById(itemRequest.packagingId())
                        .orElseThrow(() -> new ApplicationException(PACKAGING_NOT_FOUND));
            }

            int bookTotalAmount = book.getSalePrice() * itemRequest.quantity();

            OrderItem orderItem = new OrderItem(
                    orderInfo,
                    packaging,
                    book,
                    itemRequest.quantity(),
                    book.getSalePrice(),
                    bookTotalAmount
            );
            orderItemRepository.save(orderItem);

            book.decreaseStock(itemRequest.quantity());
        }

        if (member != null) {
            if (usedPoint > 0) {
                member.usePoint(usedPoint);
                PointHistory useHistory = new PointHistory(
                        orderInfo,
                        member,
                        PointType.USE,
                        usedPoint
                );
                pointHistoryRepository.save(useHistory);
            }
            if (couponIssue != null) {
                couponIssue.use();
            }
            int earnedPoint = calculateEarnedPoint(member, netAmount);
            if (earnedPoint > 0) {
                member.earnPoint(earnedPoint);
                PointHistory earnHistory = new PointHistory(
                        orderInfo,
                        member,
                        PointType.EARN,
                        earnedPoint
                );
                pointHistoryRepository.save(earnHistory);
            }
        } else {
            NonMember nonMember = new NonMember(orderInfo, request.nonMemberInfo().password());
            nonMemberRepository.save(nonMember);
        }

        return new OrderCreateDto(orderInfo.getId(), finalPaymentAmount);
    }

    private void validateBook(Book book, int requestQuantity) {
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new ApplicationException(BOOK_NOT_AVAILABLE);
        }

        if (book.getStock() < requestQuantity) {
            throw new ApplicationException(INSUFFICIENT_STOCK);
        }
    }

    private CouponIssue validateAndGetCouponIssue(Long couponIssueId, Member member, List<OrderItemCreateDto> items, int netAmount) {
        List<Long> bookIds = items.stream()
                .map(OrderItemCreateDto::bookId)
                .toList();

        List<Long> categoryIds = bookIds.stream()
                .flatMap(bookId -> bookCategoryRepository.findByBook_Id(bookId).stream())
                .map(bc -> bc.getCategory().getId())
                .distinct()
                .toList();

        CouponSearchConditionDto searchCondition = new CouponSearchConditionDto(
                member.getId(),
                categoryIds,
                bookIds
        );

        List<CouponIssue> availableCoupons = couponIssueSearchQuery.searchCouponIssue(searchCondition);

        CouponIssue couponIssue = availableCoupons.stream()
                .filter(issue -> issue.getId().equals(couponIssueId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));

        if (netAmount < couponIssue.getCoupon().getMinOrderAmount()) {
            throw new ApplicationException(COUPON_MIN_ORDER_AMOUNT_NOT_MET);
        }

        return couponIssue;
    }

    private int calculateEarnedPoint(Member member, int netAmount) {
        if (member.getRank() == null) {
            return 0;
        }

        PointPolicy pointPolicy = member.getRank().getPointPolicy();
        if (pointPolicy == null) {
            return 0;
        }

        return switch (pointPolicy.getRewardType()) {
            case RATE -> (int) Math.floor(netAmount * pointPolicy.getRewardValue() / 100.0);
            case AMOUNT -> pointPolicy.getRewardValue();
        };
    }

    private int calculateCouponDiscount(CouponIssue couponIssue, int netAmount) {
        Coupon coupon = couponIssue.getCoupon();

        if (coupon.getDiscountType() == RATE) {
            int discount = (int) Math.floor(netAmount * coupon.getDiscountValue() / 100.0);
            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
            return discount;
        } else if (coupon.getDiscountType() == AMOUNT) {
            return coupon.getDiscountValue();
        } else {
            throw new ApplicationException(INVALID_COUPON_DISCOUNT_TYPE);
        }
    }

    private LocalDate calculateShippingDate(LocalDate desiredDeliveryDate) {
        if (desiredDeliveryDate == null) {
            return LocalDate.now();
        }

        LocalDate shippingDate = desiredDeliveryDate.minusDays(1);

        if (shippingDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            shippingDate = shippingDate.minusDays(1);
        } else if (shippingDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            shippingDate = shippingDate.minusDays(2);
        }

        return shippingDate;
    }
}
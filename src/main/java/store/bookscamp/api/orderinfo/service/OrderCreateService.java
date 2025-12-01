package store.bookscamp.api.orderinfo.service;

import static store.bookscamp.api.common.exception.ErrorCode.*;
import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;
import static store.bookscamp.api.orderinfo.entity.OrderStatus.AWAITING_PAYMENT;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.delivery.entity.Delivery;
import store.bookscamp.api.delivery.repository.deliveryRepository;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.nonmember.entity.NonMember;
import store.bookscamp.api.nonmember.repository.NonMemberRepository;
import store.bookscamp.api.orderinfo.service.dto.DeliveryInfoDto;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;
import store.bookscamp.api.orderinfo.service.dto.OrderAmountDto;
import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderRequestDto;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.util.OrderNumberGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCreateService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final deliveryRepository deliveryRepository;
    private final NonMemberRepository nonMemberRepository;
    private final BookRepository bookRepository;
    private final PackagingRepository packagingRepository;
    private final MemberRepository memberRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final OrderInfoService orderInfoService;

    private final PasswordEncoder passwordEncoder; // 비회원 주문조회 비밀번호


    public OrderCreateDto createOrder(OrderRequestDto request, Long memberId) {
        log.info("[ORDER-CREATE] 주문 생성 시작 - memberId: {}, items: {}", memberId, request.items().size());

        // 회원 검증
        Member member = validateAndGetMember(memberId, request.nonMemberInfo());
        log.info("[ORDER-CREATE] 회원 검증 완료 - isMember: {}", member != null);

        DeliveryPolicy deliveryPolicy = getDeliveryPolicy();
        log.info("[ORDER-CREATE] 배송 정책 조회 완료");

        // 금액 계산
        OrderAmountDto amounts = calculateOrderAmounts(request.items(), deliveryPolicy);
        log.info("[ORDER-CREATE] 금액 계산 완료 - netAmount: {}, totalAmount: {}", amounts.netAmount(), amounts.totalAmount());

        // 쿠폰 할인
        CouponIssue couponIssue = null;
        int couponDiscountAmount = 0;
        if (request.couponIssueId() != null) {
            log.info("[ORDER-CREATE] 쿠폰 처리 시작 - couponIssueId: {}", request.couponIssueId());
            if (member == null) {
                throw new ApplicationException(COUPON_NOT_ALLOWED_FOR_NON_MEMBER);
            }
            couponIssue = validateAndGetCouponIssue(request.couponIssueId(), member, request.items(), amounts.netAmount());
            int applicableAmount = calculateApplicableAmount(couponIssue.getCoupon(), request.items(), amounts.netAmount());
            couponDiscountAmount = calculateCouponDiscount(couponIssue, applicableAmount);
            log.info("[ORDER-CREATE] 쿠폰 할인 계산 완료 - discountAmount: {}", couponDiscountAmount);
        }

        // 포인트 검증
        int usedPoint = validateAndGetUsedPoint(request.usedPoint(), member);
        log.info("[ORDER-CREATE] 포인트 검증 완료 - usedPoint: {}", usedPoint);

        // 최종 결제 금액 계산
        int finalPaymentAmount = Math.max(amounts.totalAmount() - couponDiscountAmount - usedPoint, 0);
        log.info("[ORDER-CREATE] 최종 결제 금액 계산 완료 - finalPaymentAmount: {}", finalPaymentAmount);

        // 주문 저장 (결제 대기 상태)
        OrderInfo orderInfo = saveOrder(request, member, deliveryPolicy, amounts, couponIssue, couponDiscountAmount, usedPoint, finalPaymentAmount);
        log.info("[ORDER-CREATE] 주문 저장 완료 - orderId: {}, orderNumber: {}", orderInfo.getId(), orderInfo.getOrderNumber());

        // 주문 아이템 저장 (재고 차감 없음)
        saveOrderItems(request.items(), orderInfo);
        log.info("[ORDER-CREATE] 주문 아이템 저장 완료 - itemCount: {}", request.items().size());

        // 비회원 정보만 저장 (포인트/쿠폰 처리는 결제 승인 시)
        if (member == null) {
            processNonMember(orderInfo, request.nonMemberInfo());
            log.info("[ORDER-CREATE] 비회원 정보 저장 완료");
        }

        log.info("[ORDER-CREATE] 주문 생성 성공 - orderNumber: {}, finalAmount: {}", orderInfo.getOrderNumber(), finalPaymentAmount);
        return new OrderCreateDto(orderInfo.getId(), orderInfo.getOrderNumber(), finalPaymentAmount);
    }

    private Member validateAndGetMember(Long memberId, NonMemberInfoDto nonMemberInfo) {
        if (memberId != null) {
            return memberRepository.findById(memberId)
                    .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
        }

        if (nonMemberInfo == null) {
            throw new ApplicationException(NON_MEMBER_INFO_REQUIRED);
        }

        return null;
    }

    private DeliveryPolicy getDeliveryPolicy() {
        return deliveryPolicyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ApplicationException(DELIVERY_POLICY_NOT_FOUND));
    }

    private OrderAmountDto calculateOrderAmounts(List<OrderItemCreateDto> items, DeliveryPolicy deliveryPolicy) {
        int netAmount = 0;
        int packagingFee = 0;

        for (OrderItemCreateDto itemRequest : items) {
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

        return new OrderAmountDto(netAmount, packagingFee, deliveryFee, totalAmount);
    }

    private int validateAndGetUsedPoint(Integer usedPointRequest, Member member) {
        int usedPoint = (usedPointRequest != null) ? usedPointRequest : 0;

        if (usedPoint > 0) {
            if (member == null) {
                throw new ApplicationException(POINT_NOT_ALLOWED_FOR_NON_MEMBER);
            }
            if (member.getPoint() < usedPoint) {
                throw new ApplicationException(INSUFFICIENT_POINT);
            }
        }

        return usedPoint;
    }

    private OrderInfo saveOrder(OrderRequestDto request, Member member, DeliveryPolicy deliveryPolicy,
                                 OrderAmountDto amounts, CouponIssue couponIssue, int couponDiscountAmount, int usedPoint, int finalPaymentAmount) {
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

        String orderNumber = OrderNumberGenerator.generate();

        OrderInfo orderInfo = new OrderInfo(
                orderNumber,
                member,
                couponIssue,
                delivery,
                amounts.netAmount(),
                amounts.totalAmount(),
                amounts.deliveryFee(),
                amounts.packagingFee(),
                couponDiscountAmount,
                finalPaymentAmount,
                AWAITING_PAYMENT,
                usedPoint
        );
        return orderInfoRepository.save(orderInfo);
    }

    private void saveOrderItems(List<OrderItemCreateDto> items, OrderInfo orderInfo) {
        for (OrderItemCreateDto itemRequest : items) {
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
        }
    }

    private void processNonMember(OrderInfo orderInfo, NonMemberInfoDto nonMemberInfo) {
//        String encodedPassword = passwordEncoder.encode(nonMemberInfo.password());  // 비밀번호 암호화
//        NonMember nonMember = new NonMember(orderInfo, encodedPassword); // nonMemberInfo.password()
        NonMember nonMember = new NonMember(orderInfo, nonMemberInfo.password());
        nonMemberRepository.save(nonMember);
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
        CouponIssue couponIssue = couponIssueRepository.findById(couponIssueId)
                .orElseThrow(() -> new ApplicationException(COUPON_NOT_FOUND));

        List<Long> bookIds = items.stream()
                .map(OrderItemCreateDto::bookId)
                .toList();

        boolean isAvailable = orderInfoService.isAvailableCoupon(couponIssueId, member, bookIds, netAmount);

        if (!isAvailable) {
            throw new ApplicationException(COUPON_NOT_FOUND);
        }

        return couponIssue;
    }

    private int calculateCouponDiscount(CouponIssue couponIssue, int netAmount) {
        return orderInfoService.calculateCouponDiscount(couponIssue, netAmount);
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

    private int calculateApplicableAmount(Coupon coupon, List<OrderItemCreateDto> items, int netAmount) {
        if (coupon.getTargetType() == WELCOME || coupon.getTargetType() == BIRTHDAY) {
            return netAmount;
        }

        return items.stream()
                .filter(item -> orderInfoService.isApplicableItem(item.bookId(), coupon.getTargetType(), coupon.getTargetId()))
                .mapToInt(item -> orderInfoService.calculateItemAmount(item.bookId(), item.quantity()))
                .sum();
    }
}
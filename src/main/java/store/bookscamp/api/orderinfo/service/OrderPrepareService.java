package store.bookscamp.api.orderinfo.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.orderinfo.service.dto.CouponDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareRequestDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemDto;
import store.bookscamp.api.orderinfo.service.dto.OrderItemRequestDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareDto;
import store.bookscamp.api.orderinfo.service.dto.PackagingDto;
import store.bookscamp.api.orderinfo.service.dto.PriceDto;
import store.bookscamp.api.packaging.repository.PackagingRepository;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import static store.bookscamp.api.coupon.entity.TargetType.BIRTHDAY;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPrepareService {

    private final BookRepository bookRepository;
    private final BookImageService bookImageService;
    private final PackagingRepository packagingRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final MemberRepository memberRepository;
    private final OrderInfoService orderInfoService;


    public OrderPrepareDto prepare(OrderPrepareRequestDto request, Long memberId) {
        List<OrderItemDto> orderItems = new ArrayList<>();
        int netAmount = 0;

        for (OrderItemRequestDto itemRequest : request.items()) {
            Book book = bookRepository.findById(itemRequest.bookId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.BOOK_NOT_FOUND));

            validateBook(book, itemRequest.quantity());

            int bookTotalAmount = book.getSalePrice() * itemRequest.quantity();
            netAmount += bookTotalAmount;

            String bookImageUrl = bookImageService.getThumbnailUrl(book.getId());

            orderItems.add(new OrderItemDto(
                    book.getId(),
                    book.getTitle(),
                    bookImageUrl,
                    book.getSalePrice(),
                    itemRequest.quantity(),
                    bookTotalAmount,
                    book.isPackable()
            ));
        }

        DeliveryPolicy deliveryPolicy = getActiveDeliveryPolicy();
        int deliveryFee = calculateDeliveryFee(netAmount, deliveryPolicy);

        int totalAmount = netAmount + deliveryFee;

        PriceDto priceInfo = new PriceDto(
                netAmount,
                deliveryFee,
                totalAmount,
                deliveryPolicy.getFreeDeliveryThreshold()
        );

        int availablePoint = 0;
        List<CouponDto> availableCoupons = new ArrayList<>();
        if (memberId != null) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));
            availablePoint = member.getPoint();

            List<Long> bookIds = request.items().stream()
                    .map(OrderItemRequestDto::bookId)
                    .toList();

            availableCoupons = getAvailableCoupons(member, bookIds, request.items(), netAmount);
        }

        List<PackagingDto> availablePackagings = getAvailablePackagings();

        return new OrderPrepareDto(orderItems, priceInfo, availablePoint, availablePackagings, availableCoupons);
    }

    private void validateBook(Book book, int requestQuantity) {
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new ApplicationException(ErrorCode.BOOK_NOT_AVAILABLE);
        }

        if (book.getStock() < requestQuantity) {
            throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    private int calculateDeliveryFee(int netAmount, DeliveryPolicy policy) {
        if (netAmount >= policy.getFreeDeliveryThreshold()) {
            return 0;
        }
        return policy.getBaseDeliveryFee();
    }

    private DeliveryPolicy getActiveDeliveryPolicy() {
        return deliveryPolicyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.DELIVERY_POLICY_NOT_FOUND));
    }

    private List<PackagingDto> getAvailablePackagings() {
        return packagingRepository.findAll().stream()
                .map(p -> new PackagingDto(p.getId(), p.getName(), p.getPrice()))
                .toList();
    }

    private List<CouponDto> getAvailableCoupons(Member member, List<Long> bookIds, List<OrderItemRequestDto> items, int netAmount) {
        List<CouponIssue> couponIssues = orderInfoService.getAvailableCouponIssues(member, bookIds, netAmount);

        return convertToCouponDtos(couponIssues, items, netAmount);
    }

    private List<CouponDto> convertToCouponDtos(List<CouponIssue> couponIssues, List<OrderItemRequestDto> items, int netAmount) {
        return couponIssues.stream()
                .map(issue -> createCouponDto(issue, items, netAmount))
                .toList();
    }

    private CouponDto createCouponDto(CouponIssue issue, List<OrderItemRequestDto> items, int netAmount) {
        Coupon coupon = issue.getCoupon();
        int applicableAmount = calculateApplicableAmount(coupon, items, netAmount);
        int expectedDiscount = orderInfoService.calculateCouponDiscount(issue, applicableAmount);

        return new CouponDto(
                issue.getId(),
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                expectedDiscount
        );
    }

    private int calculateApplicableAmount(Coupon coupon, List<OrderItemRequestDto> items, int netAmount) {
        if (coupon.getTargetType() == WELCOME || coupon.getTargetType() == BIRTHDAY) {
            return netAmount;
        }

        return items.stream()
                .filter(item -> orderInfoService.isApplicableItem(item.bookId(), coupon.getTargetType(), coupon.getTargetId()))
                .mapToInt(item -> orderInfoService.calculateItemAmount(item.bookId(), item.quantity()))
                .sum();
    }

}
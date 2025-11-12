package store.bookscamp.api.orderinfo.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
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
import store.bookscamp.api.couponissue.repository.CouponIssueRepository;
import store.bookscamp.api.couponissue.query.CouponIssueSearchQuery;
import store.bookscamp.api.couponissue.query.dto.CouponSearchConditionDto;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPrepareService {

    private final BookRepository bookRepository;
    private final BookImageService bookImageService;
    private final BookCategoryRepository bookCategoryRepository;
    private final PackagingRepository packagingRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final MemberRepository memberRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueSearchQuery couponIssueSearchQuery;


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

            availableCoupons = getAvailableCoupons(member, bookIds, totalAmount);
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

    private List<CouponDto> getAvailableCoupons(Member member, List<Long> bookIds, int totalAmount) {
        LocalDateTime now = LocalDateTime.now();

        List<Long> categoryIds = bookIds.stream()
                .flatMap(bookId -> bookCategoryRepository.findByBook_Id(bookId).stream())
                .map(bookCategory -> bookCategory.getCategory().getId())
                .distinct()
                .toList();

        CouponSearchConditionDto searchCondition = new CouponSearchConditionDto(
                member.getId(),
                categoryIds,
                bookIds
        );

        return couponIssueSearchQuery.searchCouponIssue(searchCondition).stream()
                .filter(issue -> issue.getUsedAt() == null)
                .filter(issue -> issue.getExpiredAt().isAfter(now))
                .map(CouponIssue::getCoupon)
                .filter(coupon -> totalAmount >= coupon.getMinOrderAmount())
                .map(coupon -> {
                    int expectedDiscount = calculateExpectedDiscount(coupon, totalAmount);

                    return new CouponDto(
                            coupon.getId(),
                            buildCouponName(coupon),
                            coupon.getDiscountType().name(),
                            coupon.getDiscountValue(),
                            coupon.getMinOrderAmount(),
                            coupon.getMaxDiscountAmount(),
                            expectedDiscount
                    );
                })
                .toList();
    }

    private String buildCouponName(Coupon coupon) {
        StringBuilder name = new StringBuilder();

        if (coupon.getMinOrderAmount() > 0) {
            name.append(String.format("%,d원 이상 구매 시 ", coupon.getMinOrderAmount()));
        }

        if (coupon.getDiscountType() == DiscountType.RATE) {
            name.append(String.format("%d%% 할인", coupon.getDiscountValue()));
            if (coupon.getMaxDiscountAmount() != null) {
                name.append(String.format(" (최대 %,d원)", coupon.getMaxDiscountAmount()));
            }
        } else {
            name.append(String.format("%,d원 할인", coupon.getDiscountValue()));
        }

        return name.toString();
    }

    private int calculateExpectedDiscount(Coupon coupon, int totalAmount) {
        if (totalAmount < coupon.getMinOrderAmount()) {
            return 0;
        }

        if (coupon.getDiscountType() == DiscountType.RATE) {
            int discount = (int) Math.floor(totalAmount * coupon.getDiscountValue() / 100.0);

            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }

            return discount;
        } else {
            return coupon.getDiscountValue();
        }
    }

}
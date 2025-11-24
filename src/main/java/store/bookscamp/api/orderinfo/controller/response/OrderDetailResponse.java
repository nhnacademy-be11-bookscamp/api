package store.bookscamp.api.orderinfo.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse (
    Long orderId,
    LocalDateTime orderDate,
    String orderStatus,

    // 주문 상품 목록
    List<OrderDetailItemResponse> items,

    // 배송정보
    String recipientName,
    String recipientPhone,
    String deliveryAddress, // 도로명 + 상세주소 + zipCode
    String deliveryMemo, // 배송메모

    int productAmount, // 상품 금액 합계 (netAmount)
    int deliveryFee,
    int packagingFee,
    int discountAmount,
    int usedPoint, // 사용 포인트
    int finalPaymentAmount // 최종 결제 금액

) {

    public record OrderDetailItemResponse(
            Long bookId,
            String bookTitle,
            int orderQuantity,
            int bookPrice,
            int bookTotalAmount
    ) {
    }
}

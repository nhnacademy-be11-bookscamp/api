package store.bookscamp.api.orderinfo.entity;

public enum OrderStatus {

    AWAITING_PAYMENT,  // 결제 대기 (주문서 생성 완료, 결제 미완료)
    PENDING,           // 배송 준비 (결제 완료)
    SHIPPING,          // 배송 중
    DELIVERED,         // 배송 완료
    RETURNED,          // 반품
    CANCELLED          // 취소
}

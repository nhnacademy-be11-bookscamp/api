package store.bookscamp.api.payment.adapter;

public interface PaymentAdapter {

    /**
     * 결제 승인
     * @param paymentKey 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 승인된 결제 정보
     */
    PaymentApprovalResponse approve(String paymentKey, String orderId, int amount);

    /**
     * 결제 취소
     * @param paymentKey 결제 키
     * @param cancelReason 취소 사유
     * @return 취소된 결제 정보
     */
    PaymentCancelResponse cancel(String paymentKey, String cancelReason);
}
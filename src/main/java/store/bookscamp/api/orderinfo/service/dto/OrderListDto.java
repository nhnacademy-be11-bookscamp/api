package store.bookscamp.api.orderinfo.service.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import store.bookscamp.api.orderinfo.entity.OrderStatus;

@Getter
@ToString
@AllArgsConstructor
public class OrderListDto {

    private Long orderId; // 주문 번호
    private LocalDateTime orderDate; // 주문 날짜
    private OrderStatus orderStatus; // 주문 상태

    private String representativeBookTitle; // 대표로 보여질 책 제목

    private int totalQuantity; // 전체 구매 권수

    private int finalPaymentAmount; // 최종 결제 금액
}

package store.bookscamp.api.orderinfo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.orderinfo.controller.request.NonMemberInfoRequest;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.service.OrderDetailService;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class NonMemberOrderController {

    private final OrderDetailService orderDetailService;

    /**
     * 비회원 주문 상세 조회
     * 주문번호(order_number) + 비밀번호
     */
    @PostMapping("/orders/non-member/{orderNumber}")
    public ResponseEntity<OrderDetailResponse> getNonMemberOrderList(
            @PathVariable String orderNumber,
            @RequestBody @Valid NonMemberInfoRequest request
    ) {

        NonMemberInfoDto dto = new NonMemberInfoDto(request.password());

        OrderDetailResponse detail =  orderDetailService.getNonMemberOrderDetail(orderNumber, dto);

        return ResponseEntity.ok(detail);

    }
}

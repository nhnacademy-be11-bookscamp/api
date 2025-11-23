package store.bookscamp.api.orderinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderListResponse;
import store.bookscamp.api.orderinfo.service.OrderDetailService;
import store.bookscamp.api.orderinfo.service.OrderListService;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderListController {

    private final OrderListService orderListService;
    private final OrderDetailService orderDetailService;

    /**
     * 주문 내역을 목록으로 조회
     */
    @GetMapping("/list")
    @RequiredRole("USER")
    public ResponseEntity<Page<OrderListResponse>> getMyOrders(
            @RequestHeader(value = "X-User-ID", required = false) Long memberId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        // 비회원 또는 인증되지 않은 경우 memberId == null
        // 비회원 주문조회 기능은 아직 없음 → 401로 처리
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        Page<OrderListDto> serviceResult = orderListService.getOrderList(memberId, pageable); // getOrderList
        Page<OrderListResponse> response = serviceResult.map(OrderListResponse::fromDto);

        return ResponseEntity.ok(response);
    }

    /**
     * 별개의 주문 내역 상세 조회
     */
    @GetMapping("/{orderId}")
    @RequiredRole("USER")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @RequestHeader(value = "X-User-ID", required = false) Long memberId,
            @PathVariable Long orderId
    ) {
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        OrderDetailResponse detail = orderDetailService.getOrderDetail(memberId, orderId);
        return ResponseEntity.ok(detail);
    }
}

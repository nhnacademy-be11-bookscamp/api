package store.bookscamp.api.orderinfo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.orderinfo.controller.request.OrderReturnRequest;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderListResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderReturnResponse;
import store.bookscamp.api.orderinfo.service.OrderDetailService;
import store.bookscamp.api.orderinfo.service.OrderListService;
import store.bookscamp.api.orderinfo.service.OrderReturnService;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;
import store.bookscamp.api.orderinfo.service.dto.OrderReturnDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderListController {

    private final OrderListService orderListService;
    private final OrderDetailService orderDetailService;
    private final OrderReturnService orderReturnService;

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

        Page<OrderListDto> serviceResult = orderListService.getOrderList(memberId, pageable);
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

    @PostMapping("/{orderId}/return")
    @RequiredRole("USER")
    public ResponseEntity<OrderReturnResponse> returnOrder(
            @RequestHeader(value = "X-User-ID", required = false) Long memberId,
            @Valid @RequestBody OrderReturnRequest request,
            @PathVariable Long orderId
    ) {
        OrderReturnDto serviceDto = orderReturnService.returnOrder(request.toDto(), orderId);

        return ResponseEntity.ok(OrderReturnResponse.fromDto(serviceDto));
    }
}
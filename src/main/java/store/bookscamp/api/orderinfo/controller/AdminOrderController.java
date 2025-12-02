package store.bookscamp.api.orderinfo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.common.pagination.RestPageImpl;
import store.bookscamp.api.orderinfo.controller.request.OrderStatusUpdateRequest;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderListResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderStatusUpdateResponse;
import store.bookscamp.api.orderinfo.service.AdminOrderListService;
import store.bookscamp.api.orderinfo.service.OrderDetailService;
import store.bookscamp.api.orderinfo.service.OrderStatusService;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;
import store.bookscamp.api.orderinfo.service.dto.OrderStatusUpdateDto;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderStatusService orderStatusService;
    private final AdminOrderListService adminOrderListService;
    private final OrderDetailService orderDetailService;

    @RequiredRole("ADMIN")
    @GetMapping
    public ResponseEntity<RestPageImpl<OrderListResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC)
            Pageable pageable
    ) {
        log.info("[ADMIN-ORDER] 전체 주문 조회 요청 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderListDto> serviceResult = adminOrderListService.getAllOrders(pageable);
        Page<OrderListResponse> responsePage = serviceResult.map(OrderListResponse::fromDto);
        RestPageImpl<OrderListResponse> response = new RestPageImpl<>(responsePage);

        log.info("[ADMIN-ORDER] 전체 주문 조회 완료 - totalElements: {}", response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @RequiredRole("ADMIN")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        log.info("[ADMIN-ORDER] 주문 상세 조회 요청 - orderId: {}", orderId);

        OrderDetailResponse response = orderDetailService.getOrderDetailForAdmin(orderId);

        log.info("[ADMIN-ORDER] 주문 상세 조회 완료 - orderId: {}", orderId);

        return ResponseEntity.ok(response);
    }

    @RequiredRole("ADMIN")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusUpdateResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        log.info("[ADMIN-ORDER] 주문 상태 변경 요청 - orderId: {}, newStatus: {}", orderId, request.orderStatus());

        OrderStatusUpdateDto dto = orderStatusService.updateOrderStatus(orderId, request.orderStatus());

        OrderStatusUpdateResponse response = OrderStatusUpdateResponse.fromDto(dto);

        log.info("[ADMIN-ORDER] 주문 상태 변경 완료 - orderId: {}, orderNumber: {}, status: {}",
                dto.orderId(), dto.orderNumber(), dto.orderStatus());

        return ResponseEntity.ok(response);
    }
}
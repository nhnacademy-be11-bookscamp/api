package store.bookscamp.api.orderinfo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.cart.cookie.CartId;
import store.bookscamp.api.orderinfo.controller.request.OrderPrepareRequest;
import store.bookscamp.api.orderinfo.controller.request.OrderCreateRequest;
import store.bookscamp.api.orderinfo.controller.response.OrderCreateResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderPrepareResponse;
import store.bookscamp.api.orderinfo.entity.OrderType;
import store.bookscamp.api.orderinfo.service.OrderCartMappingService;
import store.bookscamp.api.orderinfo.service.OrderCreateService;
import store.bookscamp.api.orderinfo.service.OrderPrepareService;
import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareDto;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderPrepareService orderPrepareService;
    private final OrderCreateService orderCreateService;
    private final OrderCartMappingService orderCartMappingService;

    @PostMapping("/prepare")
    public ResponseEntity<OrderPrepareResponse> prepare(
            @Valid @RequestBody OrderPrepareRequest request,
            @RequestHeader(value = "X-User-ID", required = false) Long memberId
    ) {
        log.info("[ORDER-CONTROLLER] POST /orders/prepare 요청 수신 - memberId: {}", memberId);
        OrderPrepareDto serviceDto = orderPrepareService.prepare(request.toDto(), memberId);
        log.info("[ORDER-CONTROLLER] POST /orders/prepare 응답 완료");
        return ResponseEntity.ok(OrderPrepareResponse.fromDto(serviceDto));
    }

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @CartId Long cartId,
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader(value = "X-User-ID", required = false) Long memberId
    ) {
        log.info("[ORDER-CONTROLLER] POST /orders 요청 수신 - memberId: {}, items: {}, orderType: {}",
                memberId, request.items().size(), request.orderType());
        OrderCreateDto serviceDto = orderCreateService.createOrder(request.toDto(), memberId);

        if (request.orderType() == OrderType.CART) {
            orderCartMappingService.saveMapping(serviceDto.orderNumber(), cartId);
        }

        log.info("[ORDER-CONTROLLER] POST /orders 응답 완료 - orderNumber: {}, finalAmount: {}",
                serviceDto.orderNumber(), serviceDto.finalPaymentAmount());
        return ResponseEntity.ok(OrderCreateResponse.fromDto(serviceDto));
    }

}
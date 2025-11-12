package store.bookscamp.api.orderinfo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.orderinfo.controller.request.OrderPrepareRequest;
import store.bookscamp.api.orderinfo.controller.request.OrderCreateRequest;
import store.bookscamp.api.orderinfo.controller.response.OrderCreateResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderPrepareResponse;
import store.bookscamp.api.orderinfo.service.OrderCreateService;
import store.bookscamp.api.orderinfo.service.OrderPrepareService;
import store.bookscamp.api.orderinfo.service.dto.OrderCreateDto;
import store.bookscamp.api.orderinfo.service.dto.OrderPrepareDto;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderPrepareService orderPrepareService;
    private final OrderCreateService orderCreateService;

    @PostMapping("/prepare")
    public ResponseEntity<OrderPrepareResponse> prepare(
            @Valid @RequestBody OrderPrepareRequest request,
            @RequestHeader(value = "X-User-ID", required = false) Long memberId
    ) {
        OrderPrepareDto serviceDto = orderPrepareService.prepare(request.toDto(), memberId);
        return ResponseEntity.ok(OrderPrepareResponse.fromDto(serviceDto));
    }

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader(value = "X-User-ID", required = false) Long memberId
    ) {
        OrderCreateDto serviceDto = orderCreateService.createOrder(request.toDto(), memberId);
        return ResponseEntity.ok(OrderCreateResponse.fromDto(serviceDto));
    }
}
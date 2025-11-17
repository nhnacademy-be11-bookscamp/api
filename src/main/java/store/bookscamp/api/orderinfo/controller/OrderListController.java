package store.bookscamp.api.orderinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.orderinfo.service.OrderListService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderListController {
    private final OrderListService orderQueryService;

    /**
     * 회원 주문 내역 목록 조회 (페이징)
     */

}

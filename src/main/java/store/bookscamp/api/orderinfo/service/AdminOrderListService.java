package store.bookscamp.api.orderinfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderListService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    public Page<OrderListDto> getAllOrders(Pageable pageable) {
        Page<OrderInfo> orderInfoPage = orderInfoRepository.findAll(pageable);

        return orderInfoPage.map(this::toOrderListDto);
    }

    private OrderListDto toOrderListDto(OrderInfo orderInfo) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderInfo.getId());

        String representationBookTitle =
                orderItemRepository.findRepresentativeBookTitleIncludingDeleted(orderInfo.getId());

        int totalQuantity = orderItems.stream()
                .mapToInt(OrderItem::getOrderQuantity)
                .sum();

        return new OrderListDto(
                orderInfo.getId(),
                orderInfo.getCreatedAt(),
                orderInfo.getOrderStatus(),
                representationBookTitle,
                totalQuantity,
                orderInfo.getFinalPaymentAmount()
        );
    }
}
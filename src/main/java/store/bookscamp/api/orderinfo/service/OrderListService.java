package store.bookscamp.api.orderinfo.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.OrderListDto;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderListService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    public Page<OrderListDto> getOrderList(Long memberId, Pageable pageable) {
        Page<OrderInfo> orderInfoPage = orderInfoRepository.findByMemberId(memberId, pageable); // 아이템

        List<OrderListDto> content = orderInfoPage.getContent().stream()
                .map(this::toOrderListDto)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderInfoPage.getTotalElements());

    }

    private OrderListDto toOrderListDto(OrderInfo orderInfo) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderInfo.getId());

        String representationBookTitle = null;
        int totalQuantity = 0;

        if (!orderItems.isEmpty()) {
            OrderItem firstItem = orderItems.get(0);
            if (firstItem.getBook() != null) {
                representationBookTitle = firstItem.getBook().getTitle();
            }

            totalQuantity = orderItems.stream()
                    .mapToInt(OrderItem::getOrderQuantity)
                    .sum();
        }

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
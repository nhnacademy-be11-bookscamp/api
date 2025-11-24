package store.bookscamp.api.orderinfo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse.OrderDetailItemResponse;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderDetailService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {

        OrderInfo orderInfo = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        if (memberId == null || !orderInfo.getMember().getId().equals(memberId)) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderId);

        List<OrderDetailItemResponse> itemResponses = orderItems.stream()
                .map(item -> new OrderDetailItemResponse(
                        item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getOrderQuantity(),
                        item.getBookPrice(),
                        item.getBookTotalAmount()
                ))
                .collect(Collectors.toList());

        String recipientName = null;
        String recipientPhone = null;
        String deliveryAddress = null;
        String deliveryMemo = null;

        if(orderInfo.getDelivery() != null) {
            var delivery = orderInfo.getDelivery();
            recipientName = delivery.getRecipientName();
            recipientPhone = delivery.getRecipientPhone();
            deliveryAddress = delivery.getRoadNameAddress() + " " + delivery.getDetailAddress();
            deliveryMemo = delivery.getDeliveryMemo();

        }
        int productAmount = orderInfo.
                getNetAmount();
        int deliveryFee = orderInfo.getDeliveryFee();
        int packagingFee = orderInfo.getPackagingFee();
        int discountAmount = orderInfo.getDiscountAmount();
        int usedPoint = orderInfo.getUsedPoint();
        int finalPaymentAmount = orderInfo.getFinalPaymentAmount();

        return new OrderDetailResponse(
                orderInfo.getId(),
                orderInfo.getCreatedAt(),
                orderInfo.getOrderStatus().name(),
                itemResponses,
                recipientName,
                recipientPhone,
                deliveryAddress,
                deliveryMemo,
                productAmount,
                deliveryFee,
                packagingFee,
                discountAmount,
                usedPoint,
                finalPaymentAmount
        );
    }
}

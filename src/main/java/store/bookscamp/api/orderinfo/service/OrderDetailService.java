package store.bookscamp.api.orderinfo.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.nonmember.entity.NonMember;
import store.bookscamp.api.nonmember.repository.NonMemberRepository;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse;
import store.bookscamp.api.orderinfo.controller.response.OrderDetailResponse.OrderDetailItemResponse;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.orderinfo.repository.OrderInfoRepository;
import store.bookscamp.api.orderinfo.service.dto.NonMemberInfoDto;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.orderitem.repository.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderDetailService {

    // 로그인한 회원
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    // 비회원 주문 조회용
    private final NonMemberRepository nonMemberRepository;
    //private final PasswordEncoder passwordEncoder;

    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {

        OrderInfo orderInfo = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        if (memberId == null || !orderInfo.getMember().getId().equals(memberId)) {
            throw new ApplicationException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderId);

        return toOrderDetailResponse(orderInfo, orderItems);
    }

<<<<<<< HEAD
=======
    public OrderDetailResponse getOrderDetailForAdmin(Long orderId) {
        OrderInfo orderInfo = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderId);

        return toOrderDetailResponse(orderInfo, orderItems);
    }

>>>>>>> ddba3c09e4bbf95bade7403bc78214ae00f98e11


//        List<OrderDetailItemResponse> itemResponses = orderItems.stream()
//                .map(item -> new OrderDetailItemResponse(
//                        item.getBook().getId(),
//                        item.getBook().getTitle(),
//                        item.getOrderQuantity(),
//                        item.getBookPrice(),
//                        item.getBookTotalAmount()
//                ))
//                .collect(Collectors.toList());
//
//        String recipientName = null;
//        String recipientPhone = null;
//        String deliveryAddress = null;
//        String deliveryMemo = null;
//
//        if(orderInfo.getDelivery() != null) {
//            var delivery = orderInfo.getDelivery();
//            recipientName = delivery.getRecipientName();
//            recipientPhone = delivery.getRecipientPhone();
//            deliveryAddress = delivery.getRoadNameAddress() + " " + delivery.getDetailAddress();
//            deliveryMemo = delivery.getDeliveryMemo();
//
//        }
//        int productAmount = orderInfo.
//                getNetAmount();
//        int deliveryFee = orderInfo.getDeliveryFee();
//        int packagingFee = orderInfo.getPackagingFee();
//        int discountAmount = orderInfo.getDiscountAmount();
//        int usedPoint = orderInfo.getUsedPoint();
//        int finalPaymentAmount = orderInfo.getFinalPaymentAmount();
//
//        return new OrderDetailResponse(
//                orderInfo.getId(),
//                orderInfo.getCreatedAt(),
//                orderInfo.getOrderStatus().name(),
//                itemResponses,
//                recipientName,
//                recipientPhone,
//                deliveryAddress,
//                deliveryMemo,
//                productAmount,
//                deliveryFee,
//                packagingFee,
//                discountAmount,
//                usedPoint,
//                finalPaymentAmount
//        );


    // 비회원부분
    public OrderDetailResponse getNonMemberOrderDetail(String orderNumber, NonMemberInfoDto nonMemberInfoDto) {

        NonMember nonMember = nonMemberRepository.findByOrderInfo_OrderNumber(orderNumber)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND));

        // TODO : 비밀번호 검증 어떻게 하는건데....???
//        String rawPassword = nonMemberInfoDto.password();
//        String encodedPassword = nonMember.getPassword();
//
//        if( !passwordEncoder.matches(rawPassword, encodedPassword) ) {
//            throw new ApplicationException(ErrorCode.ORDER_PASSWORD_INVALID);
//        }

        OrderInfo orderInfo = nonMember.getOrderInfo();
        List<OrderItem> orderItems = orderItemRepository.findByOrderInfoId(orderInfo.getId());

        return toOrderDetailResponse(orderInfo, orderItems);
    }

    private OrderDetailResponse toOrderDetailResponse(OrderInfo orderInfo, List<OrderItem> orderItems) {

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

        int productAmount = orderInfo.getNetAmount();
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

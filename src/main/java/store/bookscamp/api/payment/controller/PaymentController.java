package store.bookscamp.api.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.payment.controller.request.PaymentCancelRequest;
import store.bookscamp.api.payment.controller.request.PaymentConfirmRequest;
import store.bookscamp.api.payment.controller.response.PaymentConfirmResponse;
import store.bookscamp.api.payment.entity.Payment;
import store.bookscamp.api.payment.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        log.info("[PAYMENT-CONFIRM] orderNumber={}, paymentKey={}, amount={}",
                request.orderNumber(), request.paymentKey(), request.amount());

        Payment payment = paymentService.confirmPayment(
                request.paymentKey(),
                request.orderNumber(),
                request.amount()
        );

        PaymentConfirmResponse response = new PaymentConfirmResponse(
                payment.getId(),
                payment.getOrderInfo().getId(),
                payment.getPaidAmount(),
                payment.getPaidAt()
        );

        log.info("[PAYMENT-CONFIRM] SUCCESS - paymentId={}, orderId={}, paidAmount={}",
                payment.getId(), payment.getOrderInfo().getId(), payment.getPaidAmount());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        log.info("[PAYMENT-CANCEL] orderId={}, cancelReason={}",
                request.orderId(), request.cancelReason());

        paymentService.cancelPayment(
                request.orderId(),
                request.cancelReason()
        );

        log.info("[PAYMENT-CANCEL] SUCCESS - orderId={}", request.orderId());

        return ResponseEntity.ok().build();
    }
}
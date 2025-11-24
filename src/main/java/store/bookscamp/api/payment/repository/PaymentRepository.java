package store.bookscamp.api.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.payment.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderInfo(OrderInfo orderInfo);

    boolean existsByOrderInfo(OrderInfo orderInfo);
}
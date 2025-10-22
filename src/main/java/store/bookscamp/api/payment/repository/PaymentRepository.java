package store.bookscamp.api.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
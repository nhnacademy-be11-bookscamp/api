package store.bookscamp.api.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}

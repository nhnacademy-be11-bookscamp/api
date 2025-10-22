package store.bookscamp.api.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem cart(Cart cart);
}

package store.bookscamp.api.cart.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteAllByCart(Cart cart);

    List<CartItem> findAllByCart(Cart cart);
}

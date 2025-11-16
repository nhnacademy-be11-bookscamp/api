package store.bookscamp.api.cart.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteAllByCart(Cart cart);

    List<CartItem> findAllByCartId(Long cartId);

    Optional<CartItem> findByCartAndBook(Cart cart, Book book);

    long countCartItemByCartId(Long cartId);
}

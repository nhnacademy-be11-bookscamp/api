package store.bookscamp.api.cart.service;

import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUNd;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.entity.Cart;
import store.bookscamp.api.cart.entity.CartItem;
import store.bookscamp.api.cart.repository.CartItemRepository;
import store.bookscamp.api.cart.repository.CartRepository;
import store.bookscamp.api.cart.service.dto.CartItemAddDto;
import store.bookscamp.api.common.exception.ApplicationException;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;

    public Long addCartItem(CartItemAddDto dto) {
        Cart cart = cartRepository.findById(dto.cartId())
                .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));
        Book book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));
        CartItem cartItem = new CartItem(cart, book, dto.quantity());

        return cartItemRepository.save(cartItem)
                .getId();
    }

    public void updateCart(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException(CART_ITEM_NOT_FOUNd));
        cartItem.updateQuantity(quantity);
    }

    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException(CART_ITEM_NOT_FOUNd));
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));

        cartItemRepository.deleteAllByCart(cart);
    }
}

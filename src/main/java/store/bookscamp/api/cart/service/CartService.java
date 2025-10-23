package store.bookscamp.api.cart.service;

import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CART_ITEM_NOT_FOUNd;
import static store.bookscamp.api.common.exception.ErrorCode.CART_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.MEMBER_NOT_FOUND;

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
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;

    public Long addCartItem(CartItemAddDto dto) {
        Cart cart = cartRepository.findById(dto.cartId())
                .orElseThrow(() -> new ApplicationException(CART_NOT_FOUND));
        Book book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));
        CartItem cartItem = new CartItem(cart, book, dto.quantity());

        return cartItemRepository.save(cartItem)
                .getId();
    }

    public Long createCart(Long memberId) {
        if (memberId != null) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new ApplicationException(MEMBER_NOT_FOUND));
            return cartRepository.save(new Cart(member)).getId();
        }

        return cartRepository.save(new Cart(null)).getId();
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

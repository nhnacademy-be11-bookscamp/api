package store.bookscamp.api.cart.query;

import static store.bookscamp.api.book.entity.QBook.book;
import static store.bookscamp.api.bookimage.entity.QBookImage.bookImage;
import static store.bookscamp.api.cart.entity.QCartItem.cartItem;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;
import store.bookscamp.api.cart.service.dto.CartItemDto;

@Component
public class CartItemSearchQuery {

    private final JPAQueryFactory queryFactory;

    public CartItemSearchQuery(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public CartItemDto searchCartItemById(Long cartItemId) {
        return queryFactory
                .select(Projections.constructor(
                        CartItemDto.class,
                        cartItem.id,
                        book.id,
                        bookImage.imageUrl,
                        cartItem.quantity,
                        book.regularPrice,
                        book.salePrice,
                        cartItem.quantity.multiply(book.salePrice)
                ))
                .from(cartItem)
                .join(cartItem.book, book)
                .leftJoin(bookImage)
                .on(bookImage.book.eq(book)
                        .and(bookImage.isThumbnail.isTrue()))
                .where(cartItem.id.eq(cartItemId))
                .fetchOne();
    }

    public List<CartItemDto> searchCartItemsByCartId(Long cartId) {
        return queryFactory
                .select(Projections.constructor(
                        CartItemDto.class,
                        cartItem.id,
                        book.id,
                        bookImage.imageUrl,
                        cartItem.quantity,
                        book.regularPrice,
                        book.salePrice,
                        cartItem.quantity.multiply(book.salePrice)
                ))
                .from(cartItem)
                .join(cartItem.book, book)
                .leftJoin(bookImage)
                .on(bookImage.book.eq(book)
                        .and(bookImage.isThumbnail.isTrue()))
                .where(cartItem.cart.id.eq(cartId))
                .fetch();
    }
}

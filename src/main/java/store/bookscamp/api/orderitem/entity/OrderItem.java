package store.bookscamp.api.orderitem.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.orderinfo.entity.OrderInfo;
import store.bookscamp.api.packaging.entity.Packaging;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class OrderItem {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private OrderInfo orderInfo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "packaging_id")
    private Packaging packaging;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private Integer orderQuantity = 1;

    @Column(nullable = false)
    private int packageQuantity;

    @Column(nullable = false)
    private Integer bookTotalAmount;
}

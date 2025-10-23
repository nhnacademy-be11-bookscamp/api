package store.bookscamp.api.bookimage.entity;

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

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class BookImage {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isThumbnail;

    public BookImage(Book book, String imageUrl, Boolean isThumbnail) {
        this.book = book;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }
}

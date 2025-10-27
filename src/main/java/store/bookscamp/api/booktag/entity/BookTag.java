package store.bookscamp.api.booktag.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.tag.entity.Tag;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class BookTag {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public BookTag(Book book, Tag tag) {
        this.book = book;
        this.tag = tag;
    }
}

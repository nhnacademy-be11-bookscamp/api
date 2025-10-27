package store.bookscamp.api.book.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.common.entity.BaseEntity;
import store.bookscamp.api.contributor.entity.Contributor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 제목
    @Column(nullable = false)
    private String title;

    // 목차
    @Lob
    private String content;

    // 설명
    @Lob
    private String explanation;

    // 출판사
    @Column(nullable = false)
    private String publisher;

    // 출판일자
    @Column(nullable = false)
    private LocalDate publishDate;

    // ISBN
    @Column(length = 13, unique = true, nullable = false)
    private String isbn;

    // 정가
    @Column(nullable = false)
    private Integer regularPrice;

    // 판매가
    @Column(nullable = false)
    private Integer salePrice;

    // 수량
    @Column(nullable = false)
    private Integer stock = 100;

    // 조회수
    @Column(nullable = false)
    private long viewCount;

    // 포장 가능 여부
    @Column(nullable = false)
    private boolean packable;

    // 판매 가능 여부
    @Enumerated(STRING)
    @Column(nullable = false)
    private BookStatus status;

    // 기여자아이디
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contributor_id", nullable = false)
    private Contributor contributor;

    public Book(String title,
                String explanation,
                String content,
                String publisher,
                LocalDate publishDate,
                String isbn,
                Contributor contributor,
                BookStatus status,
                Boolean packable,
                Integer regularPrice,
                Integer salePrice,
                Integer stock,
                long viewCount
    ) {
        this.title = title;
        this.explanation = explanation;
        this.content = content;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.isbn = isbn;
        this.contributor = contributor;
        this.status = status;
        this.packable = packable;
        this.regularPrice = regularPrice;
        this.salePrice = salePrice;
        this.stock = stock;
        this.viewCount = viewCount;
    }
}

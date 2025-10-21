package store.bookcamp.api.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookcamp.api.contributor.entity.Contributor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
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
    @Column(name = "publish_date", nullable = false)
    private LocalDate publishDate;

    // ISBN
    @Column(length = 13, unique = true, nullable = false)
    private String isbn;

    // 정가
    @Column(name = "regular_price", nullable = false)
    private Integer regularPrice;

    // 판매가
    @Column(name = "sale_price", nullable = false)
    private Integer salePrice;

    // 수량
    @Column(nullable = false)
    private Integer stock = 0;

    // 조회수
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    // 포장 가능 여부
    @Column(nullable = false)
    private Boolean packable = false;

    // 판매 가능 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    // 기여자아이디
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id", nullable = false)
    private Contributor contributor;

    public Book(String title, String explanation, String content, String publisher, LocalDate publishDate, String ISBN, Contributor contributor, BookStatus status, Boolean packable, Integer regularPrice, Integer salePrice, Integer stock, Long viewCount) {
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

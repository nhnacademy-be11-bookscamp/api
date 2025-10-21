package store.bookcamp.api.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book")
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
    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    // ISBN
    @Column(length = 13, unique = true, nullable = false)
    private String ISBN;

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
    private Integer viewCount = 0;

    // 포장 가능 여부
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean packable;

    // 판매 가능 여부
    @Column(nullable = false)
    private BookStatus status;

    // 기여자아이디
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id", nullable = false)
    private Contributor contributor;
}

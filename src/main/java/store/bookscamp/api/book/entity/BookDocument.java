package store.bookscamp.api.book.entity;

import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "#{@environment.getProperty('elasticsearch.index.name')}", createIndex = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDocument {
    @Id
    private Long id;
    private String title;
    private String explanation;
    private String content;
    private String publisher;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private LocalDate publishDate;
    private String isbn;
    private String contributors;
    private Integer regularPrice;
    private Integer salePrice;
    private Integer stock;
    private long viewCount; // 인기도 정렬용
    private boolean packable;
    private String status;
    private double averageRating; // 평점 정렬용
    private long reviewCount; // 리뷰 정렬용
    //private String tags;
    //private String reviews;
}

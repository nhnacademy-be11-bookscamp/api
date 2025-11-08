package store.bookscamp.api.book.entity;

import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "bookscamp", createIndex = false)
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
    private long viewCount;
    private boolean packable;
    private String status;
    private double averageRating;
    private long reviewCount;
    private String tags;
    private String reviews;
}

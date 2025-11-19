package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSortDto {

    private Long id;
    private String title;
    private String publisher;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private LocalDate publishDate;
    private String contributors;
    private boolean packable;
    private Integer regularPrice;
    private Integer salePrice;
    private Integer stock;
    private long viewCount;
    private String isbn;
    private double averageRating;
    private long reviewCount;
    private String aiRecommand;
    private Integer aiRank;

    // 기존 record의 from(Book)
    public static BookSortDto from(Book book) {
        return BookSortDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .contributors(book.getContributors())
                .packable(book.isPackable())
                .regularPrice(book.getRegularPrice())
                .salePrice(book.getSalePrice())
                .stock(book.getStock())
                .viewCount(book.getViewCount())
                .isbn(null)
                .averageRating(0.0)
                .reviewCount(0L)
                .aiRecommand("")
                .build();
    }

    // 기존 record의 fromDocument(BookDocument)
    public static BookSortDto fromDocument(BookDocument doc) {
        return BookSortDto.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .publisher(doc.getPublisher())
                .publishDate(doc.getPublishDate())
                .contributors(doc.getContributors())
                .salePrice(doc.getSalePrice())
                .stock(doc.getStock())
                .viewCount(doc.getViewCount())
                .isbn(doc.getIsbn())
                .averageRating(doc.getAverageRating())
                .reviewCount(doc.getReviewCount())
                .packable(doc.isPackable())
                .regularPrice(doc.getRegularPrice())
                .aiRecommand("")
                .build();
    }
}

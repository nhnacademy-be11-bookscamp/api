package store.bookscamp.api.book.entity;

import org.springframework.data.annotation.Id;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import store.bookscamp.api.book.service.dto.BookSortDto;

@Document(indexName = "bookscamp-caching")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCaching {

    @Id
    private String keyword;

    private List<BookSortDto> books;

    private Long cachedAt;                // TTL용
}

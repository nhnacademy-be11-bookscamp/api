package store.bookscamp.api.book.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookSearchRequest {
    @NotBlank
    private String query;
    private String queryType;   // Title/Author/Publisher/Keyword (기본 Keyword)
    @Min(1) private Integer start;
    @Min(1) private Integer maxResults;
    private String sort;        // Accuracy/PublishTime 등
}

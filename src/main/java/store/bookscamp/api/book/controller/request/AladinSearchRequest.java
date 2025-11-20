package store.bookscamp.api.book.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AladinSearchRequest {
    @NotBlank
    private String query;
    private String queryType;
    @Min(1) private Integer start;
    @Min(1) private Integer maxResults;
    private String sort;
}

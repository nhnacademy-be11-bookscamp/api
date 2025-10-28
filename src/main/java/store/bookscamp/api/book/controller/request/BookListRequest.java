package store.bookscamp.api.book.controller.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class BookListRequest {
    @NotBlank
    private String queryType;   // Bestseller, NewItem, BlogBest ...
    private Integer categoryId; // 선택
    @Min(1) private Integer start = 1;
    @Min(1) private Integer maxResults = 10;
}


package store.bookscamp.api.book.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.ISBN;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String contributors;

    @NotBlank
    private String publisher;

    @ISBN
    private String isbn;

    @NotNull
    private LocalDate publishDate;

    @NotNull
    private Integer regularPrice;

    @NotNull
    private Integer salePrice;

    @NotNull
    private Integer stock;

    private boolean packable;

    private String content;
    private String explanation;

    private List<String> imageUrls;
    private List<Long>  tagIds;
    private Long categoryId;
}

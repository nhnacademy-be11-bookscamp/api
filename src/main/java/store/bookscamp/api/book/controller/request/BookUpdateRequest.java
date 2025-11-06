package store.bookscamp.api.book.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.ISBN;
import store.bookscamp.api.book.entity.BookStatus;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateRequest {

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

    private List<Long>  tagIds;
    private Long categoryId;
    private List<String> imageUrls;
    private List<String> removedUrls;
    private BookStatus status;
}

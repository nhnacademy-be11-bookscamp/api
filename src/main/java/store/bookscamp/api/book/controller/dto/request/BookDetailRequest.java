package store.bookscamp.api.book.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookDetailRequest {
    @NotBlank
    private String isbn13;
}

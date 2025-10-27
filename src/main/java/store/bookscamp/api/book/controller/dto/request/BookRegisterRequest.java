package store.bookscamp.api.book.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRegisterRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String contributors;

    @NotBlank
    private String publisher;

    @NotBlank
    private String isbn;

    @NotNull
    private LocalDate publishDate;

    @NotNull
    private Integer regularPrice;

    @NotNull
    private Integer salePrice;

    private String content;
    private String explanation;

   /* // 확장 필드
    private List<String> imageUrls;
    private List<Long> categoryIds;
    private List<String> tagNames;*/
}

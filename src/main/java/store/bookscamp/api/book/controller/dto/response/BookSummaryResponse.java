package store.bookscamp.api.book.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import store.bookscamp.api.book.service.dto.AladinItem;
@Data
@Builder
public class BookSummaryResponse {
    private String title;
    private String publisher;
    private String publishDate;
    private String isbn13;
    private Integer regularPrice;
    private Integer salePrice;
    private String cover;
    private String categoryName;

    public static BookSummaryResponse from(AladinItem i){
        return BookSummaryResponse.builder()
                .title(i.getTitle())
                .publisher(i.getPublisher())
                .publishDate(i.getPubDate())
                .isbn13(i.getIsbn13())
                .regularPrice(i.getPriceStandard())
                .salePrice(i.getPriceSales())
                .cover(i.getCover())
                .categoryName(i.getCategoryName())
                .build();
    }
}

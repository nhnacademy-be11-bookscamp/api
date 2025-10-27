package store.bookscamp.api.book.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import store.bookscamp.api.book.service.dto.AladinItem;

@Data
@Builder
public class BookDetailResponse {
    private String title;
    private String author;
    private String publisher;
    private String publishDate;
    private String isbn13;
    private Integer regularPrice;
    private Integer salePrice;
    private String cover;
    private String categoryName;
    private String explanation; // description
    private String content;     // toc

    public static BookDetailResponse from(AladinItem i){
        return BookDetailResponse.builder()
                .title(i.getTitle())
                .author(i.getAuthor())
                .publisher(i.getPublisher())
                .publishDate(i.getPubDate())
                .isbn13(i.getIsbn13())
                .regularPrice(i.getPriceStandard())
                .salePrice(i.getPriceSales())
                .cover(i.getCover())
                .categoryName(i.getCategoryName())
                .explanation(i.getDescription())
                .content(i.getToc())
                .build();
    }
}

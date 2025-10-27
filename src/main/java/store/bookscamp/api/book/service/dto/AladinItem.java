package store.bookscamp.api.book.service.dto;

import lombok.Data; /** 개별 아이템 */
@Data
public class AladinItem {
    private String title;
    private String author;
    private String publisher;
    private String pubDate;       // yyyy-MM-dd 또는 yyyy-MM
    private String isbn13;        // 상세/식별용 (ItemLookUp 시 핵심)
    private Integer priceStandard;
    private Integer priceSales;
    private String cover;         // 표지 URL
    private String description;   // 설명(=explanation로 매핑)
    private String toc;           // 목차(=content로 매핑)
    private String categoryName;
}

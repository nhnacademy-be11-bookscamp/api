package store.bookscamp.api.book.service.dto;


import lombok.Data;
import java.util.List;

/** 공통 래퍼 (ItemSearch, ItemList 공통 구조) */
@Data
public class AladinResponse {
    private String version;
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    private List<AladinItem> item;
}


package store.bookscamp.api.book.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AladinResponse {
    private String version;
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    private List<AladinItem> item;
}


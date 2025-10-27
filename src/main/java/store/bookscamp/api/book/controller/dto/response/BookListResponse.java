package store.bookscamp.api.book.controller.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookListResponse {
    private int total;
    private int start;
    private int count;
    private List<BookSummaryResponse> items;
}

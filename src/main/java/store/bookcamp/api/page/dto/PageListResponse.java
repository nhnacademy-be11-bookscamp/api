package store.bookcamp.api.page.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import store.bookcamp.api.page.domain.Pagenation;

@Data
@Builder
public class PageListResponse {
    private List<PageResponse> page;

    private Pagenation pagenation;
}

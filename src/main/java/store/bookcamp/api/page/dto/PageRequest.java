package store.bookcamp.api.page.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {

    private String query;

    private Integer page = 1;

    @Min(value = 1, message = "MaxResults는 1 이상이어야 합니다.")
    @Max(value = 100, message = "MaxResults는 100 이하이어야 합니다.")
    private Integer maxResults = 10;
}

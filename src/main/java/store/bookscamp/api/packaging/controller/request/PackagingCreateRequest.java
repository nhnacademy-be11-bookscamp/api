package store.bookscamp.api.packaging.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagingCreateRequest {
    @NotBlank
    private String name;

    @Min(0)
    private Integer price;

    @NotEmpty
    @Size(max=1)
    private List<@NotBlank String> imageUrl;

    public List<String> getImageUrls() { return imageUrl; }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrl = (imageUrls != null) ? imageUrls : new ArrayList<>();
    }

    /** 서비스에서 사용할 편의 메서드: 첫 번째 이미지 반환 */
    public String primaryImageUrl() { return imageUrl.get(0); }
}



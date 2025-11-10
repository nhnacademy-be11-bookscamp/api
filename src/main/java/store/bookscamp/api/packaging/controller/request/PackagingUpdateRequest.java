package store.bookscamp.api.packaging.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagingUpdateRequest {
    @NotBlank
    private String name;

    @Min(0)
    private Integer price;

    @Size(max = 1)
    private List<@NotBlank String> imageUrl;

    public List<String> getImageUrls() { return imageUrl; }

    /** 이미지 교체 의사가 있는지 */
    public boolean hasImagePatch() { return imageUrl != null; }

    /** 교체 시 사용할 신규 이미지(0번) */
    public String primaryImageUrlOrNull() {
        return (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl.get(0) : null;
    }
}

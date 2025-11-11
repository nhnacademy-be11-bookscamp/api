package store.bookscamp.api.packaging.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import store.bookscamp.api.packaging.entity.Packaging;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagingGetResponse {
    private Long id;
    private String name;
    private Integer price;
    private String imageUrl;

    public static PackagingGetResponse from(Packaging entity) {
        return PackagingGetResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}

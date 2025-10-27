package store.bookscamp.api.tag.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import store.bookscamp.api.tag.service.dto.TagUpdateDto;

@Getter
@Setter
@NoArgsConstructor
public class TagUpdateRequest {

    @NotBlank
    private String name;

    public static TagUpdateDto toDto(TagUpdateRequest request) {
        return new TagUpdateDto(request.getName());
    }
}

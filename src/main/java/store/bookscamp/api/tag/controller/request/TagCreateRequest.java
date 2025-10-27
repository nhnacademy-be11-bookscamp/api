package store.bookscamp.api.tag.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import store.bookscamp.api.tag.service.dto.TagCreateDto;
import store.bookscamp.api.tag.service.dto.TagUpdateDto;

@Getter
@NoArgsConstructor
public class TagCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    public static TagCreateDto toDto(TagCreateRequest tagCreateRequest) {
        return new TagCreateDto(tagCreateRequest.getName());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class TagUpdateRequest {
        @NotBlank
        private String name;

        public static TagUpdateDto toDto(TagUpdateRequest updateRequest) {
            return new TagUpdateDto(updateRequest.getName());
        }
    }
}

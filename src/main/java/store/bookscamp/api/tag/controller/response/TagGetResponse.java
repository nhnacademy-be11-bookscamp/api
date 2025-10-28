package store.bookscamp.api.tag.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import store.bookscamp.api.tag.service.dto.TagGetDto;

@Getter
@AllArgsConstructor
public class TagGetResponse {

    private Long id;
    private String name;

    public static TagGetResponse fromDto(TagGetDto dto) {
        return new TagGetResponse(dto.getId(), dto.getName());
    }
}

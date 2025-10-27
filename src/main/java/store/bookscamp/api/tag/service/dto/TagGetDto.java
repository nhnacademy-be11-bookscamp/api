package store.bookscamp.api.tag.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import store.bookscamp.api.tag.entity.Tag;

@Getter
@AllArgsConstructor
public class TagGetDto {

    private Long id;
    private String name;

    public static TagGetDto from(Tag tag) {
        return new TagGetDto(tag.getId(), tag.getName());
    }
}

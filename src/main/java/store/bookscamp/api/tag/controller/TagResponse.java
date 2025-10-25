package store.bookscamp.api.tag.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.tag.entity.Tag;

@Getter
@AllArgsConstructor
public class TagResponse {

    private Long id;
    private String name;

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }

}

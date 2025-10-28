package store.bookscamp.api.tag.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagUpdateDto {
    @NotBlank
    private String name;
}

package store.bookscamp.api.tag.controller;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagRequest {

    @NotBlank(message = "태그 이름을 넣어주세요")
    @Size(max = 255, message = "name은 50자 이하이어야 합니다.")
    private String name;

    public TagRequest(@NotBlank String name) {
        this.name = name;
    }
}

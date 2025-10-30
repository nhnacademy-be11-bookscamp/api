package store.bookscamp.api.contributor.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.contributor.entity.Contributor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContributorAddRequest {

    @NotBlank
    private String contributors;

    public Contributor toEntity() {
        return new Contributor(contributors);
    }
}

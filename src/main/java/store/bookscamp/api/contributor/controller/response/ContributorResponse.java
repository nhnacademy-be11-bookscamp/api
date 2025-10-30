package store.bookscamp.api.contributor.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.bookscamp.api.contributor.entity.Contributor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContributorResponse {

    private Long id;
    private String contributors;

    public static ContributorResponse from(Contributor contributor) {
        return new ContributorResponse(contributor.getId(), contributor.getContributors());
    }

}

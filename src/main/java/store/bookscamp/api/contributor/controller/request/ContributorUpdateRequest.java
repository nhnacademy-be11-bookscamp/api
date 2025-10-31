package store.bookscamp.api.contributor.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContributorUpdateRequest {

    @NotBlank(message = "수정할 기여자 이름은 비워둘 수 없습니다.")
    private String contributors;
}

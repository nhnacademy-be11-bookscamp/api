package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NonMemberInfoRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, max = 8, message = "비밀번호는 4~8자리여야 합니다.")
        String password
) {
}
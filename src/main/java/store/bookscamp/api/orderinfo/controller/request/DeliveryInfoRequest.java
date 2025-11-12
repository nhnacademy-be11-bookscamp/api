package store.bookscamp.api.orderinfo.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record DeliveryInfoRequest(
        @NotBlank(message = "수령인명은 필수입니다.")
        String recipientName,

        @NotBlank(message = "수령인 전화번호는 필수입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String recipientPhone,

        @NotNull(message = "우편번호는 필수입니다.")
        Integer zipCode,

        @NotBlank(message = "도로명주소는 필수입니다.")
        String roadNameAddress,

        @NotBlank(message = "상세주소는 필수입니다.")
        String detailAddress,

        LocalDate desiredDeliveryDate,

        String deliveryMemo
) {
}
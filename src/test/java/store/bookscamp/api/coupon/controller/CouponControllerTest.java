package store.bookscamp.api.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static store.bookscamp.api.coupon.entity.DiscountType.RATE;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.coupon.controller.request.CouponCreateRequest;
import store.bookscamp.api.coupon.controller.response.CouponResponse;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.service.CouponService;

@WebMvcTest(controllers = CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;


    @Test
    @DisplayName("POST /admin/coupons - 쿠폰 생성 성공 시 200 OK")
    void createCoupon_success() throws Exception {
        // given
        CouponCreateRequest req = new CouponCreateRequest(
                BOOK,
                1L,
                RATE,
                10,
                5_000,
                3_000,
                30,
                "테스트 코드"
        );
        when(couponService.createCoupon(ArgumentMatchers.any()))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(post("/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(couponService, times(1))
                .createCoupon(ArgumentMatchers.argThat(dto ->
                        dto.targetType() == BOOK &&
                                dto.targetId().equals(1L) &&
                                dto.discountType() == RATE &&
                                dto.discountValue() == 10 &&
                                dto.minOrderAmount() == 5_000 &&
                                dto.maxDiscountAmount().equals(3_000) &&
                                dto.validDays().equals(30)
                ));
    }

    @Test
    @DisplayName("GET /coupons - 비어있는 목록이면 [] 반환")
    void listCoupons_empty_returnsEmptyArray() throws Exception {
        // given
        when(couponService.listCoupons()).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(couponService, times(1)).listCoupons();
    }

    @Test
    @DisplayName("DELETE /admin/coupons/{id} - 삭제 성공 시 200 OK")
    void deleteCoupon_success() throws Exception {
        // given
        Long couponId = 42L;

        // when & then
        mockMvc.perform(delete("/admin/coupons/{couponId}", couponId)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(couponService, times(1)).deleteCoupon(couponId);
    }

    @Test
    @DisplayName("CouponResponse.from(Coupon) - 엔티티 기반 DTO 변환 검증")
    void couponResponse_from_success() {
        // given
        Coupon coupon = new Coupon(
                BOOK,
                1L,
                RATE,
                10,
                5000,
                3000,
                30,
                "테스트 쿠폰"
        );
        ReflectionTestUtils.setField(coupon, "id", 99L);

        // when
        CouponResponse response = CouponResponse.from(coupon);

        // then
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.targetType()).isEqualTo(BOOK);
        assertThat(response.targetId()).isEqualTo(1L);
        assertThat(response.discountType()).isEqualTo(RATE);
        assertThat(response.discountValue()).isEqualTo(10);
        assertThat(response.minOrderAmount()).isEqualTo(5000);
        assertThat(response.maxDiscountAmount()).isEqualTo(3000);
        assertThat(response.validDays()).isEqualTo(30);
        assertThat(response.name()).isEqualTo("테스트 쿠폰");
    }
}

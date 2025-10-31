package store.bookscamp.api.coupon.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.coupon.controller.request.CouponCreateRequest;
import store.bookscamp.api.coupon.entity.DiscountType;
import store.bookscamp.api.coupon.entity.TargetType;
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
    @DisplayName("POST /coupons - 쿠폰 생성 성공 시 200 OK")
    void createCoupon_success() throws Exception {
        // given
        CouponCreateRequest req = new CouponCreateRequest(
                TargetType.BOOK,
                1L,
                DiscountType.RATE,
                10,
                5_000,
                3_000,
                30
        );
        when(couponService.createCoupon(ArgumentMatchers.any()))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(couponService, times(1))
                .createCoupon(ArgumentMatchers.argThat(dto ->
                        dto.targetType() == TargetType.BOOK &&
                                dto.targetId().equals(1L) &&
                                dto.discountType() == DiscountType.RATE &&
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
    @DisplayName("DELETE /coupons/{id} - 삭제 성공 시 200 OK")
    void deleteCoupon_success() throws Exception {
        // given
        Long couponId = 42L;

        // when & then
        mockMvc.perform(delete("/coupons/{couponId}", couponId))
                .andExpect(status().isOk());

        verify(couponService, times(1)).deleteCoupon(couponId);
    }
}

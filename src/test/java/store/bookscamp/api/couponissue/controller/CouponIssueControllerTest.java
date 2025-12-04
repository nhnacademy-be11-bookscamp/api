package store.bookscamp.api.couponissue.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static store.bookscamp.api.coupon.entity.DiscountType.AMOUNT;
import static store.bookscamp.api.coupon.entity.DiscountType.RATE;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;
import static store.bookscamp.api.coupon.entity.TargetType.WELCOME;
import static store.bookscamp.api.couponissue.controller.status.CouponIssueStatus.USED;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.couponissue.controller.request.CouponIssueRequest;
import store.bookscamp.api.couponissue.controller.response.CouponIssueDownloadResponse;
import store.bookscamp.api.couponissue.controller.response.CouponIssueResponse;
import store.bookscamp.api.couponissue.controller.status.CouponFilterStatus;
import store.bookscamp.api.couponissue.entity.CouponIssue;
import store.bookscamp.api.couponissue.service.CouponIssueService;

@WebMvcTest(controllers = CouponIssueController.class)
class CouponIssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponIssueService couponIssueService;


    @Test
    @DisplayName("POST /coupon-issue/issue - 쿠폰 발급 성공")
    void issueCoupon_success() throws Exception {
        CouponIssueRequest req = new CouponIssueRequest(10L);
        when(couponIssueService.issueGeneralCoupon(10L, 99L)).thenReturn(100L);

        mockMvc.perform(post("/coupon-issue/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("X-User-ID", "99"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(couponIssueService).issueGeneralCoupon(10L, 99L);
    }

    @Test
    @DisplayName("GET /coupon-issue/my - 내 쿠폰 조회, Page<CouponIssueResponse> 반환")
    void getMyCoupons_success() throws Exception {
        Coupon coupon = mock(Coupon.class);
        when(coupon.getTargetType()).thenReturn(BOOK);
        when(coupon.getDiscountType()).thenReturn(AMOUNT);
        when(coupon.getDiscountValue()).thenReturn(1000);
        when(coupon.getMinOrderAmount()).thenReturn(0);
        when(coupon.getMaxDiscountAmount()).thenReturn(2000);
        when(coupon.getName()).thenReturn("테스트쿠폰");

        CouponIssue issue = mock(CouponIssue.class);
        when(issue.getId()).thenReturn(1L);
        when(issue.getCoupon()).thenReturn(coupon);
        when(issue.getExpiredAt()).thenReturn(LocalDateTime.now());
        when(issue.getUsedAt()).thenReturn(null);

        when(couponIssueService.listCouponIssue(eq(99L), eq(CouponFilterStatus.ALL), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(issue)));

        mockMvc.perform(get("/coupon-issue/my")
                        .header("X-User-ID", "99"))
                .andExpect(status().isOk());

        verify(couponIssueService).listCouponIssue(eq(99L), eq(CouponFilterStatus.ALL), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /coupon-issue/downloadable/{bookId} - 다운로드 가능한 쿠폰 목록 조회")
    void getDownloadableCoupons_success() throws Exception {
        Coupon coupon = new Coupon(BOOK, 1L, RATE, 10,
                1000, 2000, 30, "다운로드쿠폰");

        when(couponIssueService.findDownloadableCoupons(99L, 50L))
                .thenReturn(List.of(coupon));

        mockMvc.perform(get("/coupon-issue/downloadable/{bookId}", 50L)
                        .header("X-User-ID", "99"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(couponIssueService).findDownloadableCoupons(99L, 50L);
    }

    @Test
    @DisplayName("DELETE /coupon-issue/{id} - 쿠폰 삭제 성공")
    void deleteCouponIssue_success() throws Exception {

        mockMvc.perform(delete("/coupon-issue/{couponIssueId}", 10L)
                        .header("X-User-ID", "99"))
                .andExpect(status().isOk());

        verify(couponIssueService).deleteCouponIssue(99L, 10L);
    }

    @Test
    void couponIssueResponse_status_used() {
        Coupon coupon = new Coupon(
                WELCOME,
                1L,
                AMOUNT,
                1000,
                0,
                0,
                null,
                "welcome"
        );
        CouponIssue issue = mock(CouponIssue.class);

        when(issue.getCoupon()).thenReturn(coupon);
        when(issue.getUsedAt()).thenReturn(LocalDateTime.now());
        when(issue.getExpiredAt()).thenReturn(null);

        CouponIssueResponse res = CouponIssueResponse.from(issue);

        assertThat(res.status()).isEqualTo(USED);
    }

    @Test
    @DisplayName("CouponIssueDownloadResponse.from - RATE 타입, max 할인 금액 존재")
    void couponIssueDownloadResponse_rateWithMax() {
        // given
        Coupon coupon = new Coupon(
                BOOK,
                1L,
                RATE,
                10,
                5000,
                3000,
                30,
                "10% 할인 쿠폰"
        );
        ReflectionTestUtils.setField(coupon, "id", 100L);

        // when
        CouponIssueDownloadResponse res = CouponIssueDownloadResponse.from(coupon);

        // then
        assertThat(res.couponId()).isEqualTo(100L);
        assertThat(res.name()).isEqualTo("10% 할인 쿠폰");
        assertThat(res.discountInfo()).isEqualTo("10% 할인 (최대 3000원)");
    }


    @Test
    @DisplayName("CouponIssueDownloadResponse.from - RATE 타입, max 할인 금액 없는 경우")
    void couponIssueDownloadResponse_rateNoMax() {
        // given
        Coupon coupon = new Coupon(
                BOOK,
                1L,
                RATE,
                15,
                2000,
                null,
                15,
                "15% 할인"
        );

        // when
        CouponIssueDownloadResponse res = CouponIssueDownloadResponse.from(coupon);

        // then
        assertThat(res.discountInfo()).isEqualTo("15% 할인");
    }


    @Test
    @DisplayName("CouponIssueDownloadResponse.from - AMOUNT 타입 할인")
    void couponIssueDownloadResponse_amount() {
        // given
        Coupon coupon = new Coupon(
                BOOK,
                1L,
                AMOUNT,
                3000,
                5000,
                null,
                10,
                "3000원 할인"
        );

        // when
        CouponIssueDownloadResponse res = CouponIssueDownloadResponse.from(coupon);

        // then
        assertThat(res.discountInfo()).isEqualTo("3000원 할인");
    }
}

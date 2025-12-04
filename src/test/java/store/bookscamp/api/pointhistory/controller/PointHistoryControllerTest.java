package store.bookscamp.api.pointhistory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import store.bookscamp.api.pointhistory.entity.PointHistory;
import store.bookscamp.api.pointhistory.entity.PointType;
import store.bookscamp.api.pointhistory.service.PointHistoryService;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PointHistoryController.class)
class PointHistoryControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PointHistoryService pointHistoryService;

    String earnJson = """
            {
                "memberId": 1,
                "orderId": 10,
                "pointAmount": 500,
                "description": "설명"
            }
            """;

    String useJson = """
            {
                "memberId": 1,
                "orderId": 11,
                "pointAmount": 300,
                "description": "사용"
            }
            """;

    @Test
    @DisplayName("포인트 적립 성공")
    void earn_success() throws Exception {
        mvc.perform(post("/point-histories/earn")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(earnJson))
                .andExpect(status().isOk());

        verify(pointHistoryService, times(1)).earnPoint(any(), any());
    }

    @Test
    @DisplayName("포인트 적립 - validation 실패")
    void earn_validation_fail() throws Exception {
        String invalid = """
                {
                    "memberId": null,
                    "pointAmount": -1
                }
                """;

        mvc.perform(post("/point-histories/earn")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void use_success() throws Exception {
        mvc.perform(post("/point-histories/use")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(useJson))
                .andExpect(status().isOk());

        verify(pointHistoryService, times(1)).usePoint(any(), any());
    }

    @Test
    @DisplayName("포인트 사용 - validation 실패")
    void use_validation_fail() throws Exception {
        String invalid = """
                {
                    "memberId": null,
                    "pointAmount": -10
                }
                """;

        mvc.perform(post("/point-histories/use")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 포인트 내역 조회 성공")
    void list_success() throws Exception {

        PointHistory ph = new PointHistory(
                null, null, PointType.EARN, 100, "적립"
        );

        Page<PointHistory> page = new PageImpl<>(List.of(ph));
        when(pointHistoryService.listMemberPoints(1L, PageRequest.of(0,10))).thenReturn(page);

        mvc.perform(get("/member/point-histories")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk());

        verify(pointHistoryService, times(1))
                .listMemberPoints(1L, PageRequest.of(0,10));
    }

    @Test
    @DisplayName("내 포인트 내역 조회 - 빈 페이지")
    void list_empty() throws Exception {
        Page<PointHistory> empty = Page.empty();
        when(pointHistoryService.listMemberPoints(1L, PageRequest.of(0,10)))
                .thenReturn(empty);

        mvc.perform(get("/member/point-histories")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 포인트 내역 조회 - 페이지 파라미터 반영")
    void list_with_page_params() throws Exception {

        Page<PointHistory> page = Page.empty();
        when(pointHistoryService.listMemberPoints(1L, PageRequest.of(2,5)))
                .thenReturn(page);

        mvc.perform(get("/member/point-histories?page=2&size=5")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk());

        verify(pointHistoryService, times(1))
                .listMemberPoints(1L, PageRequest.of(2,5));
    }
}

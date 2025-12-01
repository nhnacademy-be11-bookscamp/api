package store.bookscamp.api.rank.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.rank.service.RankService;
import store.bookscamp.api.rank.service.dto.RankGetDto;

@WebMvcTest(controllers = RankController.class)
class RankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankService rankService;

    @Test
    @DisplayName("GET /rank - 회원 등급 조회 성공 시 200 OK")
    void getRank_success() throws Exception {
        // given
        Long memberId = 1L;
        RankGetDto rankResponse = new RankGetDto("GOLD", 5);

        when(rankService.getMemberRank(memberId))
                .thenReturn(rankResponse);

        // when & then
        mockMvc.perform(get("/rank")
                        .header("X-User-ID", String.valueOf(memberId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GOLD"))
                .andExpect(jsonPath("$.value").value(5));

        verify(rankService, times(1)).getMemberRank(memberId);
    }
}
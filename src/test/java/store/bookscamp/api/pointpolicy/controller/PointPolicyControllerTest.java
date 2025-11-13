package store.bookscamp.api.pointpolicy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.GOLD;
import static store.bookscamp.api.pointpolicy.entity.PointPolicyType.WELCOME;
import static store.bookscamp.api.pointpolicy.entity.RewardType.AMOUNT;
import static store.bookscamp.api.pointpolicy.entity.RewardType.RATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.cart.cookie.CartCookieService;
import store.bookscamp.api.pointpolicy.controller.request.PointPolicyCreateRequest;
import store.bookscamp.api.pointpolicy.controller.request.PointPolicyUpdateRequest;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.service.PointPolicyService;

@WebMvcTest(PointPolicyController.class)
class PointPolicyControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointPolicyService pointPolicyService;

    @MockitoBean
    private CartCookieService cartCookieService;

    @Test
    @DisplayName("포인트 정책 생성 성공")
    void createPointPolicy_success() throws Exception {
        // given
        PointPolicyCreateRequest request = new PointPolicyCreateRequest(WELCOME, AMOUNT, 100);
        given(pointPolicyService.createPointPolicy(any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/point-policies")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(pointPolicyService).createPointPolicy(any());
    }

    @Test
    @DisplayName("포인트 정책 수정 성공")
    void updatePointPolicy_success() throws Exception {
        // given
        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(GOLD, RATE, 5);
        doNothing().when(pointPolicyService).updatePointPolicy(any());

        // when & then
        mockMvc.perform(put("/point-policies/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(pointPolicyService).updatePointPolicy(any());
    }

    @Test
    @DisplayName("포인트 정책 삭제 성공")
    void deletePointPolicy_success() throws Exception {
        // given
        doNothing().when(pointPolicyService).deletePointPolicy(1L);

        // when & then
        mockMvc.perform(delete("/point-policies/{id}", 1L))
                .andExpect(status().isOk());

        verify(pointPolicyService).deletePointPolicy(1L);
    }

    @Test
    @DisplayName("포인트 정책 단건 조회 성공")
    void getPointPolicy_success() throws Exception {
        // given
        PointPolicy policy = new PointPolicy(WELCOME, AMOUNT, 100);
        setField(policy, "id", 1L);
        given(pointPolicyService.getPointPolicy(1L)).willReturn(policy);

        // when & then
        mockMvc.perform(get("/point-policies/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointPolicyId").value(1L))
                .andExpect(jsonPath("$.pointPolicyType").value("WELCOME"))
                .andExpect(jsonPath("$.rewardType").value("AMOUNT"))
                .andExpect(jsonPath("$.rewardValue").value(100));
    }

    @Test
    @DisplayName("포인트 정책 전체 조회 성공")
    void listPointPolicies_success() throws Exception {
        // given
        PointPolicy policy1 = new PointPolicy(WELCOME, AMOUNT, 100);
        setField(policy1, "id", 1L);
        PointPolicy policy2 = new PointPolicy(GOLD, RATE, 10);
        setField(policy2, "id", 2L);

        given(pointPolicyService.listPointPolicies()).willReturn(List.of(policy1, policy2));

        // when & then
        mockMvc.perform(get("/point-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pointPolicyId").value(1L))
                .andExpect(jsonPath("$[1].pointPolicyId").value(2L))
                .andExpect(jsonPath("$[0].rewardValue").value(100))
                .andExpect(jsonPath("$[1].rewardType").value("RATE"));
    }
}

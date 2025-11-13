package store.bookscamp.api.deliverypolicy.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryPolicyServiceTest {
    private static final String ROLE_HEADER = "X-User-Role";
    private static final String ADMIN = "ADMIN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryPolicyRepository deliveryPolicyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        deliveryPolicyRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /admin/delivery-policy — ADMIN 권한으로 현재 정책을 조회한다")
    void getDeliveryPolicy_success() throws Exception {
        // given
        DeliveryPolicy policy = new DeliveryPolicy(0, 3000);
        deliveryPolicyRepository.save(policy);

        // when & then
        mockMvc.perform(get("/admin/delivery-policy")
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(0))
                .andExpect(jsonPath("$.baseDeliveryFee").value(3000));
    }

    @Test
    @DisplayName("POST /admin/delivery-policy — ADMIN 권한으로 정책을 수정한다")
    void updateDeliveryPolicy_success() throws Exception {
        // given
        DeliveryPolicy policy = new DeliveryPolicy(0, 3000);
        deliveryPolicyRepository.save(policy);

        DeliveryPolicyUpdateRequest req = new DeliveryPolicyUpdateRequest();
        req.setFreeDeliveryThreshold(30000);
        req.setBaseDeliveryFee(2500);

        String json = objectMapper.writeValueAsString(req);

        // when & then
        mockMvc.perform(post("/admin/delivery-policy")
                        .header(ROLE_HEADER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(30000))
                .andExpect(jsonPath("$.baseDeliveryFee").value(2500));

        // 실제 DB도 바뀌었는지 확인
        DeliveryPolicy updated = deliveryPolicyRepository
                .findTopByOrderByIdAsc()
                .orElseThrow();

        org.assertj.core.api.Assertions.assertThat(updated.getFreeDeliveryThreshold()).isEqualTo(30000);
        org.assertj.core.api.Assertions.assertThat(updated.getBaseDeliveryFee()).isEqualTo(2500);
    }
}

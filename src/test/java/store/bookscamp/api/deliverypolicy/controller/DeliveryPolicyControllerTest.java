package store.bookscamp.api.deliverypolicy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
 import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyCreateRequest;
import store.bookscamp.api.deliverypolicy.controller.request.DeliveryPolicyUpdateRequest;
import store.bookscamp.api.deliverypolicy.entity.DeliveryPolicy;
import store.bookscamp.api.deliverypolicy.repository.DeliveryPolicyRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryPolicyControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryPolicyRepository deliveryPolicyRepository;

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String ADMIN_ROLE = "ADMIN";

    @BeforeEach
    void setUp() {
        deliveryPolicyRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /admin/delivery-policy - 컨트롤러를 통해 배송비 정책 생성 성공")
    void create_success() throws Exception {
        // given
        DeliveryPolicyCreateRequest request = new DeliveryPolicyCreateRequest();
        ReflectionTestUtils.setField(request, "freeDeliveryThreshold", 20000);
        ReflectionTestUtils.setField(request, "baseDeliveryFee", 3000);

        // when & then
        mockMvc.perform(post("/admin/delivery-policy")
                        .header(ROLE_HEADER, ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(20000))
                .andExpect(jsonPath("$.baseDeliveryFee").value(3000));
    }

    @Test
    @DisplayName("GET /admin/delivery-policy - 컨트롤러를 통해 현재 배송비 정책 조회 성공")
    void getDeliveryPolicy_success() throws Exception {
        // given
        DeliveryPolicy existing = new DeliveryPolicy(0, 3000);
        deliveryPolicyRepository.save(existing);

        // when & then
        mockMvc.perform(get("/admin/delivery-policy")
                        .header(ROLE_HEADER, ADMIN_ROLE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(0))
                .andExpect(jsonPath("$.baseDeliveryFee").value(3000));
    }

    @Test
    @DisplayName("PUT /admin/delivery-policy - 컨트롤러를 통해 배송비 정책 수정 성공")
    void update_success() throws Exception {
        // given: 기존 정책 하나 저장
        DeliveryPolicy existing = new DeliveryPolicy(0, 3000);
        deliveryPolicyRepository.save(existing);

        DeliveryPolicyUpdateRequest request = new DeliveryPolicyUpdateRequest();
        ReflectionTestUtils.setField(request, "freeDeliveryThreshold", 30000);
        ReflectionTestUtils.setField(request, "baseDeliveryFee", 2500);

        // when & then
        mockMvc.perform(put("/admin/delivery-policy")
                        .header(ROLE_HEADER, ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeDeliveryThreshold").value(30000))
                .andExpect(jsonPath("$.baseDeliveryFee").value(2500));
    }
}
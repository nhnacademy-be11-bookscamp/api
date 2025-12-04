package store.bookscamp.api.address.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.address.controller.request.AddressCreateRequest;
import store.bookscamp.api.address.controller.request.AddressUpdateRequest;
import store.bookscamp.api.address.service.AddressService;
import store.bookscamp.api.address.service.dto.AddressCreateDto;
import store.bookscamp.api.address.service.dto.AddressReadDto;
import store.bookscamp.api.address.service.dto.AddressUpdateRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest {

    private static final String HEADER_USER_ID = "X-USER-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @Test
    @DisplayName("POST /member/address - 주소 생성 성공")
    void createAddress_success() throws Exception {
        // given
        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "서울시 어딘가 1로",
                12345,
                true,
                "101호"
        );

        // when & then
        mockMvc.perform(post("/member/address")
                        .header(HEADER_USER_ID, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(addressService).createMemberAddress(eq(1L), any(AddressCreateDto.class));
    }

    @Test
    @DisplayName("GET /member/address - 회원 주소 리스트 조회 성공")
    void getAddresses_success() throws Exception {
        // given
        AddressReadDto dto = new AddressReadDto(
                10L,
                "집",
                "서울시 어딘가 1로",
                12345,
                true,
                "101호"
        );
        given(addressService.getMemberAddresses(1L))
                .willReturn(List.of(dto));

        // when & then
        mockMvc.perform(get("/member/address")
                        .header(HEADER_USER_ID, "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses[0].id").value(10L))
                .andExpect(jsonPath("$.addresses[0].label").value("집"))
                .andExpect(jsonPath("$.addresses[0].road_name_address").value("서울시 어딘가 1로"))
                .andExpect(jsonPath("$.addresses[0].zip_code").value(12345))
                .andExpect(jsonPath("$.addresses[0].is_default").value(true))
                .andExpect(jsonPath("$.addresses[0].detail_address").value("101호"));
    }

    @Test
    @DisplayName("PUT /member/address/{addressId} - 회원 주소 수정 성공")
    void updateAddress_success() throws Exception {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                "회사",
                "서울시 어딘가 2로",
                54321,
                false,
                "202호"
        );

        // when & then
        mockMvc.perform(put("/member/address/{addressId}", 10L)
                        .header(HEADER_USER_ID, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(addressService)
                .updateMemberAddress(eq(1L), eq(10L), any(AddressUpdateRequestDto.class));
    }

    @Test
    @DisplayName("DELETE /member/address/{addressId} - 회원 주소 삭제 성공")
    void deleteAddress_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/member/address/{addressId}", 10L)
                        .header(HEADER_USER_ID, "1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(addressService).deleteMemberAddress(1L, 10L);
    }
}

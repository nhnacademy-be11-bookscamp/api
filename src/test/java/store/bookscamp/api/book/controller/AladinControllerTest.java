package store.bookscamp.api.book.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;
import store.bookscamp.api.book.service.AladinService;
import store.bookscamp.api.book.service.dto.AladinItem;
import store.bookscamp.api.book.service.dto.AladinResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AladinController.class)
@AutoConfigureMockMvc(addFilters = false)
class AladinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AladinService aladinService;

    @Test
    @DisplayName("알라딘 도서 리스트 조회 성공 (비동기)")
    void list_Success() throws Exception {
        AladinItem item = new AladinItem();
        item.setTitle("Test Book");

        AladinResponse response = new AladinResponse();
        response.setTotalResults(1);
        response.setStartIndex(1);
        response.setItem(List.of(item));

        // any() 사용으로 인자 매칭 유연화
        given(aladinService.fetchList(any(), any(), any(), any()))
                .willReturn(Mono.just(response));

        // 1. 비동기 요청 시작
        MvcResult mvcResult = mockMvc.perform(get("/admin/aladin/list")
                        .param("queryType", "Bestseller")
                        .param("categoryId", "1")
                        .param("start", "1")
                        .param("maxResults", "10"))
                .andExpect(request().asyncStarted()) // 비동기 시작 확인
                .andReturn();

        // 2. 비동기 결과 디스패치 및 검증
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Test Book"));
    }

    @Test
    @DisplayName("알라딘 도서 상세 조회 성공 (동기)")
    void detail_Success() throws Exception {
        // detail 메서드는 block()을 사용하여 동기적으로 동작하므로 asyncDispatch 불필요
        AladinItem item = new AladinItem();
        item.setTitle("Detail Book");
        item.setIsbn13("9781234567890");

        AladinResponse response = new AladinResponse();
        response.setItem(List.of(item));

        given(aladinService.lookupByIsbn13("9781234567890"))
                .willReturn(Mono.just(response));

        mockMvc.perform(get("/admin/aladin/books/9781234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail Book"));
    }

    @Test
    @DisplayName("알라딘 도서 상세 조회 실패 (데이터 없음) - null 반환")
    void detail_Fail_NullResponse() throws Exception {
        AladinResponse emptyResponse = new AladinResponse();
        emptyResponse.setItem(null);

        given(aladinService.lookupByIsbn13("0000000000000"))
                .willReturn(Mono.just(emptyResponse));

        mockMvc.perform(get("/admin/aladin/books/0000000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("알라딘 검색 성공 (동기)")
    void search_Success() throws Exception {
        // search 메서드도 block()을 사용하므로 동기 테스트
        AladinItem item = new AladinItem();
        item.setTitle("Search Result");

        AladinResponse response = new AladinResponse();
        response.setTotalResults(5);
        response.setItem(List.of(item));

        given(aladinService.search(any(), any(), any(), any(), any()))
                .willReturn(Mono.just(response));

        mockMvc.perform(get("/admin/aladin/search")
                        .param("query", "Java")
                        .param("queryType", "Keyword")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5));
    }

    @Test
    @DisplayName("알라딘 검색 실패 - 결과 없음 (예외 발생)")
    void search_Fail_NotFound() throws Exception {
        // block() 결과가 null이 되도록 Mono.empty() 반환
        given(aladinService.search(any(), any(), any(), any(), any()))
                .willReturn(Mono.empty());

        mockMvc.perform(get("/admin/aladin/search")
                        .param("query", "Unknown")
                        .accept(MediaType.APPLICATION_JSON))
                // Controller에서 예외를 던지므로 4xx 또는 5xx 확인
                .andExpect(status().is4xxClientError());
    }
}
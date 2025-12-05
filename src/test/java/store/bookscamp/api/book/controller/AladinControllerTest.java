package store.bookscamp.api.book.controller;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reactor.core.publisher.Mono;
import store.bookscamp.api.book.service.AladinService;
import store.bookscamp.api.book.service.dto.AladinItem;
import store.bookscamp.api.book.service.dto.AladinResponse;

import java.util.List;

@WebMvcTest(AladinController.class)
class AladinControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AladinService aladinService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /admin/aladin/list 정상 처리")
    void list_success() throws Exception {
        AladinResponse resp = new AladinResponse();
        resp.setTotalResults(100);
        resp.setStartIndex(1);
        resp.setItem(List.of());

        when(aladinService.fetchList(any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(resp));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/admin/aladin/list")
                                .param("queryType", "ItemList")
                                .param("categoryId", "1")
                                .param("start", "1")
                                .param("maxResults", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.start").value(1))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("GET /admin/aladin/books/{isbn13} 정상 처리")
    void detail_success() throws Exception {
        String isbn = "1234567890123";

        var item = new AladinItem();
        item.setTitle("Test Book");

        AladinResponse resp = new AladinResponse();
        resp.setItem(List.of(item));

        when(aladinService.lookupByIsbn13(eq(isbn)))
                .thenReturn(Mono.just(resp));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/admin/aladin/books/" + isbn)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    @DisplayName("GET /admin/aladin/books/{isbn13} - 데이터 없음 → null 반환")
    void detail_return_null() throws Exception {
        when(aladinService.lookupByIsbn13(anyString()))
                .thenReturn(Mono.just(null));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/admin/aladin/books/9999")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("")); // null → empty body
    }

    @Test
    @DisplayName("GET /admin/aladin/search 정상 처리")
    void search_success() throws Exception {
        AladinResponse resp = new AladinResponse();
        resp.setTotalResults(50);
        resp.setStartIndex(1);
        resp.setItem(List.of());

        when(aladinService.search(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Mono.just(resp));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/admin/aladin/search")
                                .param("query", "java")
                                .param("queryType", "Title")
                                .param("start", "1")
                                .param("maxResults", "10")
                                .param("sort", "Accuracy")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(50));
    }

    @Test
    @DisplayName("GET /admin/aladin/search 응답 null → BOOK_NOT_FOUND 예외")
    void search_not_found() throws Exception {

        when(aladinService.search(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Mono.justOrEmpty(null)); // Mono.empty()

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/admin/aladin/search")
                                .param("query", "xxx")
                                .param("queryType", "Title")
                                .param("start", "1")
                                .param("maxResults", "10")
                )
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode").value("BOOK_NOT_FOUND"));
    }
}

package store.bookscamp.api.cart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.controller.request.CartItemAddRequest;
import store.bookscamp.api.cart.controller.request.CartItemUpdateRequest;

@Disabled
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CartControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired BookRepository bookRepository;

    private Long bookId;

    @BeforeEach
    void setUp() {
        Book book = bookRepository.save(new Book(
                "책 제목",
                "책 설명",
                null,
                "출판사",
                LocalDate.of(2001, 1, 1),
                "123456789012",
                "기여자",
                AVAILABLE,
                false,
                20000,
                18000,
                100,
                0L
        ));
        bookId = book.getId();
    }

    @Test
    @DisplayName("비회원 장바구니 생성 및 조회 통합 테스트")
    void guestCart_flow() throws Exception {

        // 1) 장바구니 추가 (쿠키 없음 → 새 cartToken 생성)
        CartItemAddRequest addRequest = new CartItemAddRequest(bookId, 2);

        MvcResult addResponse = mockMvc.perform(
                        post("/carts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addRequest))
                ).andExpect(status().isOk())
                .andReturn();

        // 응답 Set-Cookie에서 cartToken 찾기
        String setCookie = addResponse.getResponse().getHeader("Set-Cookie");
        String cartToken = setCookie.split("cartToken=")[1].split(";")[0];

        // 2) 장바구니 조회
        mockMvc.perform(
                get("/carts")
                        .cookie(new Cookie("cartToken", cartToken))
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("장바구니 수량 업데이트")
    void updateCartItem() throws Exception {

        // 비회원 추가
        CartItemAddRequest addRequest = new CartItemAddRequest(bookId, 1);

        MvcResult addResponse = mockMvc.perform(
                post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest))
        ).andReturn();

        String setCookie = addResponse.getResponse().getHeader("Set-Cookie");
        String cartToken = setCookie.split("cartToken=")[1].split(";")[0];

        // 장바구니 조회해서 cartItemId 가져올 수도 있지만
        // 여기서는 간단히 1번 아이템 업데이트한다고 가정
        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(5);

        mockMvc.perform(
                put("/carts/1")
                        .cookie(new Cookie("cartToken", cartToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
        ).andExpect(status().isOk());
    }
}

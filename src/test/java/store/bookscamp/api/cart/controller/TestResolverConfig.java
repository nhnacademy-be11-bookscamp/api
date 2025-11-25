package store.bookscamp.api.cart.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static store.bookscamp.api.book.entity.BookStatus.AVAILABLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.cart.controller.request.CartItemAddRequest;
import store.bookscamp.api.cart.controller.request.CartItemUpdateRequest;
import store.bookscamp.api.cart.cookie.CartCookieService;
import store.bookscamp.api.cart.cookie.CartIdArgumentResolver;

@Import(TestResolverConfig.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CartApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private BookRepository bookRepository;

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

        CartItemAddRequest addRequest = new CartItemAddRequest(bookId, 2);

        MvcResult addResponse = mockMvc.perform(
                        post("/carts")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addRequest))
                ).andExpect(status().isOk())
                .andReturn();

        String setCookie = addResponse.getResponse().getHeader("Set-Cookie");
        String cartToken = setCookie.split("cartToken=")[1].split(";")[0];

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
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest))
        ).andReturn();

        String setCookie = addResponse.getResponse().getHeader("Set-Cookie");
        String cartToken = setCookie.split("cartToken=")[1].split(";")[0];

        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(5);

        mockMvc.perform(
                put("/carts/1")
                        .cookie(new Cookie("cartToken", cartToken))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
        ).andExpect(status().isOk());
    }
}

@TestConfiguration
public class TestResolverConfig implements WebMvcConfigurer {

    @Autowired
    private CartCookieService cartCookieService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CartIdArgumentResolver(cartCookieService));
    }
}

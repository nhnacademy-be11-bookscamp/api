package store.bookscamp.api.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import store.bookscamp.api.cart.config.CartCookieConfig;
import store.bookscamp.api.cart.cookie.CartCookieService;
import store.bookscamp.api.cart.service.CartService;

@Import(CartCookieConfig.class)
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartCookieService cartCookieService;

    @Test
    @DisplayName("POST /carts → 장바구니 추가 성공")
    void addCartItem() throws Exception {
        Long cartId = 10L;
        when(cartCookieService.extractCartId(any(), any())).thenReturn(cartId);

        String body = """
            {
              "bookId": 1,
              "quantity": 2
            }
            """;

        mockMvc.perform(
                MockMvcRequestBuilders.post("/carts")
                        .cookie(new Cookie("cartToken", "abc"))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /carts/{cartItemId} → 장바구니 수정 성공")
    void updateCartItem() throws Exception {
        Long cartId = 10L;
        when(cartCookieService.extractCartId(any(), any())).thenReturn(cartId);

        doNothing().when(cartService).updateCart(eq(cartId), eq(5L), eq(3));

        String body = """
            {
              "quantity": 3
            }
            """;

        mockMvc.perform(
                MockMvcRequestBuilders.put("/carts/5")
                        .cookie(new Cookie("cartToken", "abc"))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /carts/{cartItemId} → 장바구니 삭제 성공")
    void deleteCartItem() throws Exception {
        Long cartId = 10L;
        when(cartCookieService.extractCartId(any(), any())).thenReturn(cartId);

        doNothing().when(cartService).deleteCartItem(cartId, 5L);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/carts/5")
                        .cookie(new Cookie("cartToken", "abc"))
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /carts → 장바구니 조회 성공")
    void getCartItems() throws Exception {
        Long cartId = 10L;
        when(cartCookieService.extractCartId(any(), any())).thenReturn(cartId);

        when(cartService.getCartItems(cartId)).thenReturn(List.of());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/carts")
                        .cookie(new Cookie("cartToken", "abc"))
        ).andExpect(status().isOk());
    }
}

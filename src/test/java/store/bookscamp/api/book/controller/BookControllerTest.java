package store.bookscamp.api.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store.bookscamp.api.book.controller.request.AladinCreateRequest;
import store.bookscamp.api.book.controller.request.BookCreateRequest;
import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.BookSearchService;
import store.bookscamp.api.book.service.BookService;
import store.bookscamp.api.book.service.dto.*;
import store.bookscamp.api.bookimage.service.BookImageService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookSearchService bookSearchService;

    @MockitoBean
    private BookImageService bookImageService;

    @Test
    @DisplayName("수동 도서 등록 성공")
    void createBook_Success() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .title("Title")
                .contributors("Author")
                .publisher("Publisher")
                .isbn("9781234567890")
                .publishDate(LocalDate.now())
                .regularPrice(15000)
                .salePrice(13500)
                .stock(100)
                .packable(true)
                .content("Book Content")
                .explanation("Book Explanation")
                .imageUrls(List.of("http://image.url"))
                .tagIds(List.of(1L, 2L))
                .categoryId(10L)
                .build();

        mockMvc.perform(post("/admin/books/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도서 등록이 완료되었습니다."));

        verify(bookService).createBook(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("알라딘 도서 등록 성공")
    void aladinCreateBook_Success() throws Exception {
        AladinCreateRequest request = new AladinCreateRequest(
                "Aladin Title",
                "Author",
                "Pub",
                "9788966263158",
                LocalDate.now(),
                20000,
                18000,
                50,
                true,
                "Desc",
                "Explanation",
                null,
                List.of(1L),
                1L
        );

        mockMvc.perform(post("/admin/aladin/books")
                        .param("imgUrls", "http://img.url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("알라딘 도서 등록이 완료되었습니다."));

        verify(bookService).createBook(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("도서 수정 성공")
    void updateBook_Success() throws Exception {
        BookUpdateRequest request = new BookUpdateRequest(
                "New Title",
                "New Author",
                "New Pub",
                "9781234567890",
                LocalDate.now(),
                15000,
                13500,
                50,
                true,
                "New Content",
                "New Explanation",
                List.of(1L),
                2L,
                List.of("new_img.jpg"),
                List.of("old_img.jpg"),
                BookStatus.AVAILABLE
        );

        mockMvc.perform(put("/admin/books/{id}/update", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도서 정보가 수정되었습니다."));

        verify(bookService).updateBook(eq(1L), any(BookUpdateRequest.class));
    }

    @Test
    @DisplayName("도서 삭제 성공")
    void deleteBook_Success() throws Exception {
        mockMvc.perform(delete("/admin/books/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("도서 목록 검색 및 조회 성공")
    void getBooks_Success() throws Exception {
        BookSortDto dto = BookSortDto.builder()
                .id(1L)
                .title("Book1")
                .publisher("Pub")
                .publishDate(LocalDate.now())
                .contributors("Author")
                .packable(true)
                .regularPrice(10000)
                .salePrice(9000)
                .stock(50)
                .viewCount(100L)
                .isbn("9781234567890")
                .averageRating(4.5)
                .reviewCount(10L)
                .aiRecommand("AI Rec")
                .aiRank(1)
                .build();

        Page<BookSortDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        given(bookSearchService.searchBooks(any(BookSearchRequest.class))).willReturn(page);
        given(bookImageService.getThumbnailUrl(1L)).willReturn("thumb.jpg");

        mockMvc.perform(get("/books")
                        .param("keyWord", "test")
                        .param("sortType", "id")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Book1"))
                .andExpect(jsonPath("$.content[0].thumbnailUrl").value("thumb.jpg"));
    }

    @Test
    @DisplayName("도서 상세 조회 성공")
    void getBookDetail_Success() throws Exception {
        BookDetailDto detailDto = new BookDetailDto(
                1L,
                "Detail Book",
                "Explanation",
                "Content",
                "Publisher",
                LocalDate.now(),
                "Contributors",
                "9781111111111",
                BookStatus.AVAILABLE,
                true,
                12000,
                10800,
                100,
                0L,
                List.of(),
                List.of(),
                List.of()
        );
        given(bookService.getBookDetail(1L)).willReturn(detailDto);

        mockMvc.perform(get("/bookDetail/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail Book"));
    }

    @Test
    @DisplayName("추천 도서 목록 조회 성공")
    void getRecommendBooks_Success() throws Exception {
        BookIndexDto dto = new BookIndexDto(1L, "Rec Book", "Pub", "Contrib", 10000, 9000, "http://img.com");
        given(bookService.getRecommendBooks()).willReturn(List.of(dto));

        mockMvc.perform(get("/books/indexBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Rec Book"));
    }

    @Test
    @DisplayName("베스트 셀러 목록 조회 성공")
    void getBestSellers_Success() throws Exception {
        // given
        BookIndexDto bestDto = new BookIndexDto(
                10L,
                "Best Seller",
                "Best Pub",
                "Best Author",
                20000,
                18000,
                "http://best.img"
        );

        // 1. Page 객체 생성
        Page<BookIndexDto> page = new PageImpl<>(List.of(bestDto), PageRequest.of(0, 9), 1);

        // 2. Service가 반환할 Record(Dto) 생성 ✨
        BookBestSellerDto<BookIndexDto> serviceResult = BookBestSellerDto.from(page);

        // 3. Mocking: Service는 이제 Page가 아니라 Record를 반환함
        given(bookService.getBestSellers(any(Pageable.class))).willReturn(serviceResult);

        // when & then
        // Controller 내부에서 Record -> Page -> RestPageImpl 변환 로직이 수행됨
        mockMvc.perform(get("/books/best")
                        .param("page", "0")
                        .param("size", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Best Seller"))
                .andExpect(jsonPath("$.content[0].salePrice").value(18000))
                .andExpect(jsonPath("$.content[0].thumbnail").value("http://best.img"));
    }

    @Test
    @DisplayName("위시리스트 조회 성공")
    void getWishListBooks_Success() throws Exception {
        BookWishListDto dto = new BookWishListDto(1L, "Wish Book", "Pub", LocalDate.now(), "Contrib", true, 12000, 10000, BookStatus.AVAILABLE, "img.jpg");
        Page<BookWishListDto> page = new PageImpl<>(List.of(dto));

        given(bookService.getWishList(eq(100L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/wishlist")
                        .header("X-User-ID", "100")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Wish Book"));
    }

    @Test
    @DisplayName("위시리스트 삭제 성공")
    void deleteWishList_Success() throws Exception {
        mockMvc.perform(delete("/wishlist/{itemId}", 5L)
                        .header("X-User-ID", "100"))
                .andExpect(status().isOk());

        verify(bookService).deleteWishList(5L, 100L);
    }

    @Test
    @DisplayName("관리자: 쿠폰 적용 대상 도서 조회 성공")
    void getBooksForCoupon_Success() throws Exception {
        // BookService.getBooks 반환 타입이 Page<Book>이므로 Mock Book 필요
        store.bookscamp.api.book.entity.Book mockBook = org.mockito.Mockito.mock(store.bookscamp.api.book.entity.Book.class);
        given(mockBook.getId()).willReturn(10L);
        given(mockBook.getTitle()).willReturn("Coupon Book");
        // BookCouponResponse.from(book)에서 필요한 필드 모킹
        given(mockBook.getSalePrice()).willReturn(13500);
        given(mockBook.getRegularPrice()).willReturn(15000);
        given(mockBook.getPublisher()).willReturn("Pub");
        given(mockBook.getContributors()).willReturn("Author");

        Page<store.bookscamp.api.book.entity.Book> bookPage = new PageImpl<>(List.of(mockBook));

        given(bookService.getBooks(anyString(), any(Pageable.class))).willReturn(bookPage);

        mockMvc.perform(get("/admin/books/coupon")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Coupon Book"));
    }

    @Test
    @DisplayName("신간 도서 조회 성공")
    void getNewBooks_Success() throws Exception {
        // BookService.getNewBooks 반환 타입이 Page<Book>
        store.bookscamp.api.book.entity.Book mockBook = org.mockito.Mockito.mock(store.bookscamp.api.book.entity.Book.class);
        given(mockBook.getId()).willReturn(20L);
        given(mockBook.getTitle()).willReturn("New Book");
        // BookSortDto.from(book)에 필요한 필드 모킹
        given(mockBook.getContributors()).willReturn("Author");
        given(mockBook.getPublisher()).willReturn("Pub");
        given(mockBook.getPublishDate()).willReturn(LocalDate.now());
        given(mockBook.getRegularPrice()).willReturn(12000);
        given(mockBook.getSalePrice()).willReturn(10800);
        given(mockBook.getStock()).willReturn(10);
        given(mockBook.getViewCount()).willReturn(0L);
        given(mockBook.isPackable()).willReturn(true);

        Page<store.bookscamp.api.book.entity.Book> bookPage = new PageImpl<>(List.of(mockBook));

        given(bookService.getNewBooks(any(Pageable.class))).willReturn(bookPage);
        given(bookImageService.getThumbnailUrl(20L)).willReturn("new_thumb.jpg");

        mockMvc.perform(get("/books/newBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("New Book"))
                .andExpect(jsonPath("$.content[0].thumbnailUrl").value("new_thumb.jpg"));
    }
}
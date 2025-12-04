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
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.BookSearchService;
import store.bookscamp.api.book.service.BookService;
import store.bookscamp.api.book.service.dto.*;
import store.bookscamp.api.bookimage.service.BookImageService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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
        // [수정] ISBN 유효성 검사 통과를 위해 실제 ISBN 사용
        // imageUrls는 Controller에서 @RequestParam으로 받아 setter로 주입하므로 DTO 생성 시엔 null 또는 빈 리스트
        AladinCreateRequest request = new AladinCreateRequest(
                "Aladin Title",
                "Author",
                "Pub",
                "9788966263158",     // [수정] 유효한 ISBN13 (지킬박사와 하이드 씨 등 아무거나)
                LocalDate.now(),
                20000,
                18000,
                50,
                true,
                "Desc",
                "Explanation",
                null,                // imageUrls (어차피 컨트롤러에서 덮어씀)
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
        // BookUpdateRequest 생성자 (title, contributors, publisher, isbn, publishDate, regularPrice, salePrice, stock, packable, content, explanation, tagIds, categoryId, imageUrls, removedUrls, status)
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
        // BookSortDto Builder 사용
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
        // BookDetailDto 생성자 (id, title, explanation, content, publisher, publishDate, contributors, isbn, status, packable, regularPrice, salePrice, stock, viewCount, categoryList, tagList, imageUrlList)
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
        // BookIndexDto (id, title, publisher, contributors, regularPrice, salePrice, thumbnail)
        BookIndexDto dto = new BookIndexDto(1L, "Rec Book", "Pub", "Contrib", 10000, 9000, "http://img.com");
        given(bookService.getRecommendBooks()).willReturn(List.of(dto));

        mockMvc.perform(get("/books/indexBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Rec Book"));
    }

    @Test
    @DisplayName("위시리스트 조회 성공")
    void getWishListBooks_Success() throws Exception {
        // BookWishListDto (id, title, publisher, publishDate, contributors, packable, regularPrice, salePrice, status, thumbnailUrl)
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
        Book mockBook = mock(Book.class);
        given(mockBook.getId()).willReturn(10L);
        given(mockBook.getTitle()).willReturn("Coupon Book");
        given(mockBook.getSalePrice()).willReturn(13500);
        given(mockBook.getRegularPrice()).willReturn(15000);
        given(mockBook.getPublisher()).willReturn("Pub");
        given(mockBook.getContributors()).willReturn("Author");
        // BookCouponResponse.from(book) 내부에서 사용하는 Getter들을 모두 Mocking해야 함.
        // BookCouponResponse가 없지만 일반적으로 필요한 필드들 Mocking 완료.

        Page<Book> bookPage = new PageImpl<>(List.of(mockBook));

        given(bookService.getBooks(anyString(), any(Pageable.class))).willReturn(bookPage);

        mockMvc.perform(get("/admin/books/coupon")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Coupon Book"));
    }

    @Test
    @DisplayName("신간 도서 조회 성공")
    void getNewBooks_Success() throws Exception {
        Book mockBook = mock(Book.class);
        given(mockBook.getId()).willReturn(20L);
        given(mockBook.getTitle()).willReturn("New Book");
        given(mockBook.getContributors()).willReturn("Author");
        given(mockBook.getPublisher()).willReturn("Pub");
        given(mockBook.getPublishDate()).willReturn(LocalDate.now());
        given(mockBook.getRegularPrice()).willReturn(12000);
        given(mockBook.getSalePrice()).willReturn(10800);
        given(mockBook.getStock()).willReturn(10);
        given(mockBook.getViewCount()).willReturn(0L);
        given(mockBook.isPackable()).willReturn(true);
        // BookSortDto.from(book)에서 호출되는 getter들 모두 Mocking 필요

        Page<Book> bookPage = new PageImpl<>(List.of(mockBook));

        given(bookService.getNewBooks(any(Pageable.class))).willReturn(bookPage);
        given(bookImageService.getThumbnailUrl(20L)).willReturn("new_thumb.jpg");

        mockMvc.perform(get("/books/newBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("New Book"))
                .andExpect(jsonPath("$.content[0].thumbnailUrl").value("new_thumb.jpg"));
    }
}
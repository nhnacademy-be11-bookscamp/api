package store.bookscamp.api.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.book.controller.request.AladinCreateRequest;
import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.controller.response.BookIndexResponse;
import store.bookscamp.api.book.controller.response.BookInfoResponse;
import store.bookscamp.api.book.controller.response.BookSortResponse;
import store.bookscamp.api.book.controller.request.BookCreateRequest;
import store.bookscamp.api.book.service.BookSearchService;
import store.bookscamp.api.book.service.BookService;
import store.bookscamp.api.book.service.dto.BookCreateDto;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookIndexDto;
import store.bookscamp.api.book.service.dto.BookSearchRequest;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.common.pagination.RestPageImpl;


@RestController
@RequiredArgsConstructor
@Tag(name = "책 API", description = "Book API입니다")
public class BookController {

    private final BookService bookService;
    private final BookSearchService bookSearchService;
    private final BookImageService bookImageService;

    @PostMapping(value = "/admin/books/create", produces = "application/json")
    @Operation(summary = "create book", description = "수동도서등록 API")
    @RequiredRole("ADMIN")
    public ResponseEntity<String> createBook(
            @RequestBody BookCreateRequest req
    ) {

        bookService.createBook(BookCreateDto.from(req));

        return ResponseEntity.ok().body("{\"message\":\"도서 등록이 완료되었습니다.\"}");
    }

    @PostMapping(value = "/admin/aladin/books", produces = "application/json")
    @Operation(summary = "create aladin book", description = "알라딘도서등록 API")
    @RequiredRole("ADMIN")
    public ResponseEntity<String> aladinCreateBook(@RequestBody @Valid AladinCreateRequest req) {

        bookService.createBook(BookCreateDto.from(req));

        return ResponseEntity.ok().body("{\"message\":\"알라딘 도서 등록이 완료되었습니다.\"}");
    }

    // 도서 수정
    @PutMapping(value = "/admin/books/{id}/update", produces = "application/json")
    @Operation(summary = "update book", description = "도서수정 API")
    @RequiredRole("ADMIN")
    public ResponseEntity<String> updateBook(
            @PathVariable Long id,
            @RequestBody BookUpdateRequest req
    ) {

        bookService.updateBook(id, req);

        return ResponseEntity.ok().body("{\"message\":\"도서 정보가 수정되었습니다.\"}");
    }

    @GetMapping("/books")
    public ResponseEntity<RestPageImpl<BookSortResponse>> getBooks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyWord,
            @RequestParam(defaultValue = "id") String sortType,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        BookSearchRequest searchRequest = new BookSearchRequest(categoryId, keyWord, sortType, pageable);
        Page<BookSortDto> bookSortDtoPage = bookSearchService.searchBooks(searchRequest);
        List<BookSortResponse> bookSortResponseList = new ArrayList<>();

        //TODO : 성능개선 필요
        for(BookSortDto dto : bookSortDtoPage){
            String thumbnailUrl = bookImageService.getThumbnailUrl(dto.id());
            bookSortResponseList.add(BookSortResponse.from(dto,thumbnailUrl));
        }

        Page<BookSortResponse> bookSortResponsePage = new PageImpl<>(
                bookSortResponseList,
                bookSortDtoPage.getPageable(),
                bookSortDtoPage.getTotalElements()
        );
        RestPageImpl<BookSortResponse> responsePage = new RestPageImpl<>(bookSortResponsePage);

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/bookDetail/{id}")
    public ResponseEntity<BookInfoResponse> getBookDetail(@PathVariable Long id){
        BookDetailDto bookDetail = bookService.getBookDetail(id);
        BookInfoResponse from = BookInfoResponse.from(bookDetail);
        return ResponseEntity.ok(from);
    }

    @GetMapping("/books/allBooks")
    public ResponseEntity<List<BookIndexResponse>> getAllBooks(){
        List<BookIndexDto> allBooks = bookService.getAllBooks();
        List<BookIndexResponse> allBooksResponse = allBooks.stream()
                .map(BookIndexResponse::from)
                .toList();
        return ResponseEntity.ok(allBooksResponse);
    }
}


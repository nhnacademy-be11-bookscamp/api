package store.bookscamp.api.book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import store.bookscamp.api.book.controller.request.AladinCreateRequest;
import store.bookscamp.api.book.controller.response.BookInfoResponse;
import store.bookscamp.api.book.controller.response.BookSortResponse;
import store.bookscamp.api.book.controller.request.BookCreateRequest;
import store.bookscamp.api.book.service.BookService;
import store.bookscamp.api.book.service.dto.BookCreateDto;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.common.pagination.RestPageImpl;
import store.bookscamp.api.common.service.MinioService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final MinioService minioService;

    @PostMapping(value = "/admin/books/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createBook(
            @ModelAttribute BookCreateRequest req,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {

        System.out.println("출판일자: " +req.getPublishDate());

        bookService.createBook(BookCreateDto.from(req, files, minioService));

        return ResponseEntity.ok().body("{\"message\":\"도서 등록이 완료되었습니다.\"}");
    }

    @PostMapping(value = "/admin/aladin/books", produces = "application/json")
    public ResponseEntity<?> aladinCreateBook(@RequestBody @Valid AladinCreateRequest req) {
        bookService.createBook(BookCreateDto.from(req));
        return ResponseEntity.ok().body("{\"message\":\"알라딘 도서 등록이 완료되었습니다.\"}");
    }

    // todo: 도서 수정
//    @PutMapping(value = "/admin/books/{id}/update", consumes = {"multipart/form-data"})
//    public ResponseEntity<?> updateBook(
//            @PathVariable Long id,
//            @RequestPart("req") BookUpdateRequest req
//    ) {
//        bookService.updateBook() dto 변환하기
//    }

    @GetMapping("/books")
    public ResponseEntity<RestPageImpl<BookSortResponse>> getBooks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyWord,
            @RequestParam(defaultValue = "id") String sortType,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        Page<BookSortDto> bookSortDtoPage = bookService.searchBooks(categoryId, keyWord, sortType, pageable);

        Page<BookSortResponse> bookSortResponsePage = bookSortDtoPage.map(BookSortResponse::from);

        RestPageImpl<BookSortResponse> responsePage = new RestPageImpl<>(bookSortResponsePage);

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/bookDetail/{id}")
    public ResponseEntity<BookInfoResponse> getBookDetail(@PathVariable Long id){
        BookDetailDto bookDetail = bookService.getBookDetail(id);
        BookInfoResponse from = BookInfoResponse.from(bookDetail);
        return ResponseEntity.ok(from);
    }
}


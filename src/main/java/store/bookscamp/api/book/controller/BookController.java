package store.bookscamp.api.book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.book.controller.response.BookSortResponse;
import store.bookscamp.api.book.controller.request.BookCreateRequest;
import store.bookscamp.api.book.service.BookService;
import store.bookscamp.api.book.service.dto.BookSortDto;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class BookController {

    private final BookService bookService;


    @PostMapping(value = "/admin/books/create", produces = "application/json")
    public ResponseEntity<?> createBook(@RequestBody @Valid BookCreateRequest req) {
        bookService.createBook(req);
        return ResponseEntity.ok().body("{\"message\":\"도서 등록이 완료되었습니다.\"}");
    }

    @GetMapping("/books")
    public ResponseEntity<Page<BookSortResponse>> getBooks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyWord,
            @RequestParam(defaultValue = "id") String sortType,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ){
        Page<BookSortDto> bookSortDtoPage = bookService.searchBooks(categoryId, keyWord, sortType, pageable);

        // BookSortDto를 BookSortResponse로 변환, 계층분리
        Page<BookSortResponse> responsePage = bookSortDtoPage.map(dto ->
                new BookSortResponse(
                        dto.id(),
                        dto.title(),
                        dto.explanation(),
                        dto.content(),
                        dto.publisher(),
                        dto.publishDate(),
                        dto.contributor(),
                        dto.status(),
                        dto.packable(),
                        dto.regularPrice(),
                        dto.salePrice(),
                        dto.stock(),
                        dto.viewCount()
                )
        );

        return ResponseEntity.ok(responsePage);
    }
}


package store.bookscamp.api.book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.book.controller.dto.request.BookRegisterRequest;
import store.bookscamp.api.book.service.BookService;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;


    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<?> registerBook(@RequestBody @Valid BookRegisterRequest req) {
        bookService.registerBook(req);
        return ResponseEntity.ok().body("{\"message\":\"도서 등록이 완료되었습니다.\"}");
    }
}


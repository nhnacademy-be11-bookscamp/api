package store.bookscamp.api.book.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import store.bookscamp.api.book.controller.dto.response.BookDetailResponse;
import store.bookscamp.api.book.controller.dto.request.BookListRequest;
import store.bookscamp.api.book.controller.dto.response.BookListResponse;
import store.bookscamp.api.book.controller.dto.request.BookSearchRequest;
import store.bookscamp.api.book.controller.dto.response.BookSummaryResponse;
import store.bookscamp.api.book.service.AladinService;
import store.bookscamp.api.book.service.dto.AladinResponse;

@RestController
@RequestMapping(value = "/api/aladin", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Tag(name= "Aladin API")
public class AladinController {

    private final AladinService aladinService;

    // 1) 리스트 (베스트셀러 등)
    @GetMapping("/list")
    public Mono<BookListResponse> list(@Valid @ModelAttribute BookListRequest req){
        return aladinService.fetchList(req.getQueryType(), req.getCategoryId(), req.getStart(), req.getMaxResults())
                .map(this::toListResponse);
    }

    // 2) 상세 (ISBN13)
    @GetMapping("/books/{isbn13}")
    public Mono<BookDetailResponse> detail(@PathVariable String isbn13){
        return aladinService.lookupByIsbn13(isbn13)
                .map(resp -> resp.getItem() != null && !resp.getItem().isEmpty()
                        ? BookDetailResponse.from(resp.getItem().get(0))
                        : null);
    }

    // 3) 검색
    @GetMapping("/search")
    public Mono<BookListResponse> search(@Valid @ModelAttribute BookSearchRequest req){
        return aladinService.search(req.getQuery(), req.getQueryType(), req.getStart(), req.getMaxResults(), req.getSort())
                .map(this::toListResponse);
    }

    private BookListResponse toListResponse(AladinResponse resp){
        List<BookSummaryResponse> items = resp.getItem() == null ? List.of()
                : resp.getItem().stream().map(BookSummaryResponse::from).toList();
        return BookListResponse.builder()
                .total(resp.getTotalResults())
                .start(resp.getStartIndex())
                .count(items.size())
                .items(items)
                .build();
    }
}

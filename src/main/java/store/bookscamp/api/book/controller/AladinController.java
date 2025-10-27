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
@RequestMapping(value = "/aladin", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public BookDetailResponse detail(@PathVariable String isbn13){
        var resp = aladinService.lookupByIsbn13(isbn13).block(); // ✅ Mono -> AladinResponse 로 변환
        if (resp == null || resp.getItem() == null || resp.getItem().isEmpty()) {
            return null;
        }
        return BookDetailResponse.from(resp.getItem().get(0));
    }

    // 3) 검색
    @GetMapping(value ="/search",produces="application/json")
    public BookListResponse search(@Valid @ModelAttribute BookSearchRequest req){
        AladinResponse resp = aladinService
                .search(req.getQuery(), req.getQueryType(), req.getStart(), req.getMaxResults(), req.getSort())
                .block();

        return toListResponse(resp);
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

package store.bookcamp.api.page.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookcamp.api.page.dto.PageListResponse;
import store.bookcamp.api.page.repository.PageRepository;
import store.bookcamp.api.page.service.PageService;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class PageController {
    private final PageService pageService;

    /**
     * 도서 목록 조회 API
     * 예시 URL : /api/books/list?page=1&maxResults=10&query=자바
     */
    @GetMapping
    public PageListResponse getBooks(@Valid PageRequest request) {
        return pageService.getBookWithPagenation(request);
    }
}

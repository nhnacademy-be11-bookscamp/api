package store.bookcamp.api.page.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookcamp.api.page.domain.Pagenation;
import store.bookcamp.api.page.dto.PageListResponse;
import store.bookcamp.api.page.dto.PageRequest;
import store.bookcamp.api.page.dto.PageResponse;
import store.bookcamp.api.page.repository.PageRepository;

/**
 * 조회를 하는 BookService 이므로 추후 이름 수정이 필요할 수 있음
 */

@Service
@RequiredArgsConstructor
public class PageService {
    private final PageRepository pageRepository;

    /**
     * 도서 목록을 검색 조건 및 페이지네이션 처리하여 반환
     * 카테고리 조회 도는 검색어 조건에 맞게 쿼리가 동적으로 실행돼야 함
     * @param request 페이지 및 검색 조건을 담은 요청 DTO
     * @return 페이지네이션 정보와 도서 목록을 담은 응답 DTO
     */
    public PageListResponse getBookWithPagenation(PageRequest request) {
        int page = Math.max(request.getPage() -1, 0);
        int size = request.getMaxResults();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        Page<Book> bookPage;

        if(request.getQuery() != null && !request.getQuery().isEmpty()) {
            bookPage = pageRepository.findAllByTitleContaining(request.getQuery(), pageable);
        } else {
            bookPage = pageRepository.findAll(pageable);
        }

        List<PageResponse> pagenatedBooks = bookPage.getContent().stream()
                .map(PageResponse::from)
                .collect(Collectors.toList());

        Pagenation pagenation = new Pagenation(bookPage);

        return PageListResponse.builder()
                .page(pagenatedBooks)
                .pagenation(pagenation)
                .build();

    }
}

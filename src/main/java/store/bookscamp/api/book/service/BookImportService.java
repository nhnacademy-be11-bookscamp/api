package store.bookscamp.api.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.AladinItem;

@Service
@RequiredArgsConstructor
public class BookImportService {
    private final AladinService aladinService;
    private final BookRepository bookRepository;

    public Mono<Long> importByIsbn13(String isbn13, String contributor, BookStatus status, boolean packable){
        return aladinService.lookupByIsbn13(isbn13)
                .map(resp -> resp.getItem() != null && !resp.getItem().isEmpty() ? resp.getItem().get(0) : null)
                .map((AladinItem i) -> aladinService.toBookEntity(i, contributor, status, packable))
                .map(bookRepository::save)
                .map(Book::getId);
    }
}

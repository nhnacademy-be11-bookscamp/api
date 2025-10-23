package store.bookscamp.api.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookSortDto;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Page<BookSortDto> searchBooks(Long categoryId, String keyword, String sortType, Pageable pageable) {

        Page<Book> bookPage = bookRepository.getBooks(categoryId, keyword, sortType, pageable);

        // from 메서드를 통해 Dto로 변환
        Page<BookSortDto> dtoPage = bookPage.map(BookSortDto::from);

        return dtoPage;
    }
}

package store.bookscamp.api.book.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookSortDto;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public List<BookSortDto> sortByTitle(){
        List<Book> bookList = bookRepository.findAllByOrderByTitleAsc();
        List<BookSortDto> bookSortDtoList = new ArrayList<>();

        for (Book book : bookList) {
            String title = book.getTitle();
            String publisher = book.getPublisher();

            bookSortDtoList.add(new BookSortDto(title, publisher));
        }
        return bookSortDtoList;
    }

    public List<BookSortDto> sortBySalePriceAsc(){
        List<Book> salePriceAscList = bookRepository.findAllByOrderBySalePriceAsc();
        List<BookSortDto> bookSortDtoList = new ArrayList<>();

        for(Book book : salePriceAscList){
            String title = book.getTitle();
            String publisher = book.getPublisher();

            bookSortDtoList.add(new BookSortDto(title, publisher));
        }
        return bookSortDtoList;
    }

    public List<BookSortDto> sortBySalePriceDesc(){
        List<Book> salePriceDescList = bookRepository.findAllByOrderBySalePriceDesc();
        List<BookSortDto> bookSortDtoList = new ArrayList<>();

        for(Book book : salePriceDescList){
            String title = book.getTitle();
            String publisher = book.getPublisher();

            bookSortDtoList.add(new BookSortDto(title, publisher));
        }
        return bookSortDtoList;
    }
}

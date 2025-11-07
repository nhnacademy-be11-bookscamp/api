package store.bookscamp.api.bookImage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.bookimage.service.dto.BookImageCreateDto;
import store.bookscamp.api.bookimage.service.dto.BookImageDeleteDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.book.entity.Book;

import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookImageServiceTest {

    @Mock
    private BookImageRepository bookImageRepository;

    @InjectMocks
    private BookImageService bookImageService;

    private Book book;
    private BookImageCreateDto createDto;
    private BookImageDeleteDto deleteDto;

    @BeforeEach
    void setUp() {
        // 준비: Book 객체 생성
        book = new Book("Book Title", "Description", null, "Publisher", null, "1234567890", "Author", null, false, 100, 90, 10, 0L);

        // BookImageCreateDto 준비
        createDto = new BookImageCreateDto(book, Arrays.asList("url1", "url2"));

        // BookImageDeleteDto 준비
        deleteDto = new BookImageDeleteDto(1L, "url1");
    }

    @Test
    @DisplayName("createBookImage - 정상적인 이미지 생성")
    void createBookImage_success() {
        // given
        when(bookImageRepository.save(any(BookImage.class))).thenReturn(new BookImage(book, "url1", true));

        // when
        bookImageService.createBookImage(createDto);

        // then
        verify(bookImageRepository, times(2)).save(any(BookImage.class));  // 2번 호출되는지 검증
    }

    @Test
    @DisplayName("createBookImage - 책이 없는 경우 예외 발생")
    void createBookImage_bookNotFound() {
        // given
        BookImageCreateDto invalidDto = new BookImageCreateDto(null, Arrays.asList("url1", "url2"));

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> bookImageService.createBookImage(invalidDto));
        assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("deleteBookImage - 정상적으로 이미지 삭제")
    void deleteBookImage_success() {
        // given
        BookImage bookImage = new BookImage(book, "url1", true);
        when(bookImageRepository.findById(1L)).thenReturn(Optional.of(bookImage));

        // when
        bookImageService.deleteBookImage(deleteDto);

        // then
        verify(bookImageRepository, times(1)).delete(bookImage);  // 삭제가 1번 호출되는지 확인
    }

    @Test
    @DisplayName("deleteBookImage - 이미지가 없는 경우 예외 발생")
    void deleteBookImage_imageNotFound() {
        // given
        when(bookImageRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> bookImageService.deleteBookImage(deleteDto));
        assertEquals(ErrorCode.IMAGE_NOT_FOUND, exception.getErrorCode());
    }
}

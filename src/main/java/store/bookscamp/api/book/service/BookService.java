package store.bookscamp.api.book.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.controller.request.BookCreateRequest;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.bookcategory.entity.BookCategory;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.bookimage.service.dto.BookImageCreateDto;
import store.bookscamp.api.booktag.entity.BookTag;
import store.bookscamp.api.booktag.repository.BookTagRepository;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.common.service.MinioService;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.repository.TagRepository;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final BookTagRepository bookTagRepository;
    private final BookImageService bookImageService;
    private final MinioService minioService;

    @Transactional
    public void createBook(BookCreateRequest req) {

        Book book = new Book(
                req.getTitle(),
                req.getExplanation(),
                req.getContent(),
                req.getPublisher(),
                req.getPublishDate(),
                req.getIsbn(),
                req.getContributors(),
                BookStatus.AVAILABLE,
                req.isPackable(),
                req.getRegularPrice(),
                req.getSalePrice(),
                req.getStock(),
                0                          // viewCount
        );
        bookRepository.save(book);

        //img insert
        if (req.getImages() != null) {
            List<String> imgUrls = minioService.uploadFiles(req.getImages(), "book");
            BookImageCreateDto dto = new BookImageCreateDto(book, imgUrls);
            bookImageService.createBookImage(dto);
        }

        // category
        if (req.getCategoryIds() != null) {
            for (Long categoryId : req.getCategoryIds()) {
                Category categoryById = categoryRepository.getCategoryById(categoryId);
                bookCategoryRepository.save(new BookCategory(book, categoryById));
            }
        }
        // tag
        if (req.getTagIds() != null) {
            for (Long tagId : req.getTagIds()) {
                Tag tag = tagRepository.getTagById(tagId);
                bookTagRepository.save(new BookTag(book, tag));
            }
        }
    }

    public Page<BookSortDto> searchBooks(Long categoryId, String keyword, String sortType, Pageable pageable) {

        List<Long> categoryIdsToSearch = null;

        if (categoryId != null) {
            categoryIdsToSearch = categoryRepository.getAllDescendantIdsIncludingSelf(categoryId);
        }

        Page<Book> bookPage = bookRepository.getBooks(categoryIdsToSearch, keyword, sortType, pageable);
        // from 메서드를 통해 Dto로 변환
        return bookPage.map(BookSortDto::from);
    }

    public BookDetailDto getBookDetail(Long id) {
        Book book = bookRepository.getBookById(id);

        if (book==null){
            throw new ApplicationException(ErrorCode.BOOK_NOT_FOUND);
        }

        return BookDetailDto.from(book);
    }
}

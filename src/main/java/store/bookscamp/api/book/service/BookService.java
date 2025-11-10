package store.bookscamp.api.book.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookCreateDto;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookIndexDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
import store.bookscamp.api.book.service.dto.BookUpdateDto;
import store.bookscamp.api.bookcategory.entity.BookCategory;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.bookimage.service.dto.BookImageCreateDto;
import store.bookscamp.api.bookimage.service.dto.BookImageDeleteDto;
import store.bookscamp.api.booktag.entity.BookTag;
import store.bookscamp.api.booktag.repository.BookTagRepository;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.CategoryService;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
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
    private final BookImageRepository bookImageRepository;
    private final BookImageService bookImageService;
    private final CategoryService categoryService;
    private final BookIndexService bookIndexService;

    @Transactional
    public void createBook(BookCreateDto dto) {

        Book book = new Book(
                dto.title(),
                dto.explanation(),
                dto.content(),
                dto.publisher(),
                dto.publishDate(),
                dto.isbn(),
                dto.contributors(),
                BookStatus.AVAILABLE,
                dto.packable(),
                dto.regularPrice(),
                dto.salePrice(),
                dto.stock(),
                0                          // viewCount
        );
        bookRepository.saveAndFlush(book);
        bookIndexService.indexBook(book);

        if (dto.imageUrls() != null && !dto.imageUrls().isEmpty()) {
            bookImageService.createBookImage(new BookImageCreateDto(book, dto.imageUrls()));
        }

        if (dto.categoryId() != null) {
            Category category = categoryRepository.getCategoryById(dto.categoryId());
            bookCategoryRepository.save(new BookCategory(book, category));
        }

        if (dto.tagIds() != null) {
            for (Long tagId : dto.tagIds()) {
                Tag tag = tagRepository.getTagById(tagId);
                bookTagRepository.save(new BookTag(book, tag));
            }
        }
    }

    @Transactional
    public void updateBook(Long id, BookUpdateRequest req) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BOOK_NOT_FOUND));

        BookUpdateDto dto = BookUpdateDto.from(req);

        book.updateInfo(
                dto.title(),
                dto.contributors(),
                dto.publisher(),
                dto.isbn(),
                dto.publishDate(),
                dto.regularPrice(),
                dto.salePrice(),
                dto.stock(),
                dto.packable(),
                dto.content(),
                dto.explanation()
        );

        if (dto.status() != null) {
            book.setStatus(dto.status());
        }

        if (dto.removedUrls() != null && !dto.removedUrls().isEmpty()) {
            dto.removedUrls().forEach(url ->
                    bookImageRepository.findByImageUrl(url).ifPresent(image -> {
                        BookImageDeleteDto deleteDto = new BookImageDeleteDto(image.getId(), image.getImageUrl());
                        bookImageService.deleteBookImage(deleteDto);
                    })
            );
        }

        if (dto.imageUrls() != null && !dto.imageUrls().isEmpty()) {
            bookImageService.createBookImage(new BookImageCreateDto(book, dto.imageUrls()));
        }

        if (dto.categoryId() != null) {
            bookCategoryRepository.deleteByBook(book);
            Category category = categoryRepository.findById(dto.categoryId())
                            .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));

            boolean exists = bookCategoryRepository.existsByBookAndCategory(book, category);
            if (!exists) {
                bookCategoryRepository.save(new BookCategory(book, category));
            }
        }

        if (dto.tagIds() != null) {
            bookTagRepository.deleteByBook(book);
            for (Long tagId : dto.tagIds()) {
                Tag tag = tagRepository.getTagById(tagId);
                bookTagRepository.save(new BookTag(book, tag));
            }
        }
    }

    public Page<BookSortDto> getBooks(Long categoryId, String sortType, Pageable pageable) {

        List<Long> categoryIdsToSearch = null;

        if (categoryId != null) {
            categoryIdsToSearch = categoryService.getDescendantIdsIncludingSelf(categoryId);
        }

        Page<Book> bookPage = bookRepository.getBooks(categoryIdsToSearch, sortType, pageable);
        // from 메서드를 통해 Dto로 변환
        return bookPage.map(BookSortDto::from);
    }

    public BookDetailDto getBookDetail(Long id) {

        Book book = bookRepository.getBookById(id);

        if (book==null){
            throw new ApplicationException(ErrorCode.BOOK_NOT_FOUND);
        }

        book.increaseViewCount();

        Long categoryId = bookCategoryRepository.findByBook(book)
                .map(bc -> bc.getCategory().getId())
                .orElse(null);

        List<Long> tagIds = bookTagRepository.findAllByBook(book).stream()
                .map(bt -> bt.getTag().getId())
                .toList();

        List<String> imageUrls = bookImageRepository.findAllByBook(book).stream()
                .map(bi -> bi.getImageUrl())
                .toList();

        return BookDetailDto.from(book, categoryId, tagIds, imageUrls);
    }

    public List<BookIndexDto> getAllBooks() {

        List<Book> allBooksList = bookRepository.findAll();

        return allBooksList.stream().map(book -> {

            String thumbnailUrl = bookImageRepository.findByBook(book).stream()
                    .filter(BookImage::isThumbnail)
                    .map(BookImage::getImageUrl)
                    .findFirst()
                    .orElse(null);

            return new BookIndexDto(
                    book.getId(),
                    book.getTitle(),
                    book.getPublisher(),
                    book.getContributors(),
                    book.getRegularPrice(),
                    book.getSalePrice(),
                    thumbnailUrl
            );
        }).toList();
    }
}

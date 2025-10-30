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
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.booktag.repository.BookTagRepository;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.contributor.entity.Contributor;
import store.bookscamp.api.contributor.repository.ContributorRepository;
import store.bookscamp.api.tag.repository.TagRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store.bookscamp.api.book.service.dto.BookSortDto;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final ContributorRepository contributorRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BookImageRepository bookImageRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final BookTagRepository bookTagRepository;

    @Transactional
    public void createBook(BookCreateRequest req) {

        // contributor
        Contributor contributor = contributorRepository.findByContributors(req.getContributors())
                .orElseGet(() -> contributorRepository.save(new Contributor(req.getContributors())));

        Book book = new Book(
                req.getTitle(),
                req.getExplanation(),
                req.getContent(),
                req.getPublisher(),
                req.getPublishDate(),
                req.getIsbn(),
                contributor,
                BookStatus.AVAILABLE,
                false,                     // packable
                req.getRegularPrice(),
                req.getSalePrice(),
                100,                       // stock 기본값
                0                          // viewCount
        );
        bookRepository.save(book);


      /*  // bookImg
        if (req.getImageUrls() != null) {
            for (String url : req.getImageUrls()) {
                bookImageRepository.save(new BookImage(book, url, false));
            }
        }

        // category
        if (req.getCategoryIds() != null) {
            for (Long categoryId : req.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리 ID: " + categoryId));
                bookCategoryRepository.save(new BookCategory(book, category));
            }
        }

        // tag
        if (req.getTagNames() != null) {
            for (String tagName : req.getTagNames()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                bookTagRepository.save(new BookTag(book, tag));
            }
        }*/
    }
    public Page<BookSortDto> searchBooks(Long categoryId, String keyword, String sortType, Pageable pageable) {

        List<Long> categoryIdsToSearch = null;

        if (categoryId != null) {
            categoryIdsToSearch = categoryRepository.getAllDescendantIdsIncludingSelf(categoryId);
        }

        Page<Book> bookPage = bookRepository.getBooks(categoryIdsToSearch, keyword, sortType, pageable);
        // from 메서드를 통해 Dto로 변환
        Page<BookSortDto> dtoPage = bookPage.map(BookSortDto::from);

        return dtoPage;
    }

    public BookDetailDto getBookDetail(Long id){
        Book book = bookRepository.getBookById(id);
        return BookDetailDto.from(book);
    }
}

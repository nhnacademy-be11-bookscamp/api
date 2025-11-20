package store.bookscamp.api.book.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookCreateDto;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookIndexDto;
import store.bookscamp.api.book.service.dto.BookUpdateDto;
import store.bookscamp.api.book.service.dto.BookWishListDto;
import store.bookscamp.api.bookcategory.entity.BookCategory;
import store.bookscamp.api.bookcategory.repository.BookCategoryRepository;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.bookimage.service.BookImageService;
import store.bookscamp.api.bookimage.service.dto.BookImageCreateDto;
import store.bookscamp.api.bookimage.service.dto.BookImageDeleteDto;
import store.bookscamp.api.booklike.service.BookLikeService;
import store.bookscamp.api.booktag.entity.BookTag;
import store.bookscamp.api.booktag.repository.BookTagRepository;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.category.service.dto.CategoryDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.repository.TagRepository;
import store.bookscamp.api.tag.service.dto.TagDto;


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
    private final BookIndexService bookIndexService;
    private final BookLikeService bookLikeService;
    private final MemberRepository memberRepository;
    private final BookCachingIndexService bookCachingIndexService;

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

        if (dto.imageUrls() != null && !dto.imageUrls().isEmpty()) {
            bookImageService.createBookImage(new BookImageCreateDto(book, dto.imageUrls()));
        }

        if (dto.categoryId() != null) {
            Category category = categoryRepository.getCategoryById(dto.categoryId());
            bookCategoryRepository.save(new BookCategory(book, category));
            BookDocument doc = bookIndexService.mapBookToDocument(book);
            doc.setCategory(category.getName());
            bookIndexService.indexBook(doc);
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
                bookCategoryRepository.saveAndFlush(new BookCategory(book, category));
            }
            BookProjection bookProjection = bookRepository.findByIdWithRatingAndReview(book.getId());
            BookDocument doc = bookIndexService.projectionToDoc(bookProjection);
            bookIndexService.indexBook(doc);
        }

        if (dto.tagIds() != null) {
            bookTagRepository.deleteByBook(book);
            for (Long tagId : dto.tagIds()) {
                Tag tag = tagRepository.getTagById(tagId);
                bookTagRepository.save(new BookTag(book, tag));
            }
        }
        bookCachingIndexService.invalidateCachesContainingBook(id);
    }

    @Transactional
    public void deleteBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BOOK_NOT_FOUND));

        book.softDelete();
        bookIndexService.deleteBookIndex(id);
        bookCachingIndexService.invalidateCachesContainingBook(id);

        List<BookTag> bookTags = bookTagRepository.findAllByBookId(id);
        for (BookTag bt : bookTags) {
            bt.softDelete();
        }

        List<BookCategory> bookCategories = bookCategoryRepository.findAllByBookId(id);
        for (BookCategory bc : bookCategories) {
            bc.softDelete();
        }
    }

    @Transactional
    public BookDetailDto getBookDetail(Long bookId) {

        Book book = bookRepository.getBookById(bookId);

        if (book == null) {
            throw new ApplicationException(ErrorCode.BOOK_NOT_FOUND);
        }

        book.increaseViewCount();
        BookProjection bookProjection = bookRepository.findByIdWithRatingAndReview(bookId);
        BookDocument doc = bookIndexService.projectionToDoc(bookProjection);
        doc.setViewCount(book.getViewCount());
        bookIndexService.indexBook(doc);

        List<CategoryDto> categoryList = new ArrayList<>();
        List<BookCategory> bookCategoryList = bookCategoryRepository.findByBook_Id(bookId);

        for (BookCategory bookCategory : bookCategoryList) {
            categoryList.add(new CategoryDto(
                    bookCategory.getCategory().getId(),
                    bookCategory.getCategory().getName()
            ));
        }

        List<TagDto> tagList = new ArrayList<>();
        List<BookTag> bookTagList = bookTagRepository.findByBook_Id(bookId);

        for (BookTag bookTag : bookTagList) {
            tagList.add(new TagDto(
                    bookTag.getTag().getId(),
                    bookTag.getTag().getName()
            ));
        }

        List<String> imageUrlList = new ArrayList<>();
        List<BookImage> bookImageList = bookImageRepository.findByBook_Id(bookId);

        for (BookImage bookImage : bookImageList) {
            imageUrlList.add(bookImage.getImageUrl());
        }
        return BookDetailDto.from(book, categoryList, tagList, imageUrlList);
    }

    public List<BookIndexDto> getRecommendBooks() {

        List<Book> recommendBooks = bookRepository.getRecommendBooks();

        return recommendBooks.stream().map(book -> {

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

    public Page<BookWishListDto> getWishList(Long memberId, Pageable pageable) {
        if (memberId == null) {
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }

        List<Book> wishListByMemberId = getWishListByMemberId(memberId);
        List<BookWishListDto> wishListDtoList = new ArrayList<>();

        for (Book book : wishListByMemberId) {
            String thumbnailUrl = bookImageService.getThumbnailUrl(book.getId());
            wishListDtoList.add(new BookWishListDto(
                    book.getId(),
                    book.getTitle(),
                    book.getPublisher(),
                    book.getPublishDate(),
                    book.getContributors(),
                    book.isPackable(),
                    book.getRegularPrice(),
                    book.getSalePrice(),
                    book.getStatus(),
                    thumbnailUrl
            ));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), wishListDtoList.size());

        List<BookWishListDto> pagedList = wishListDtoList.subList(start, end);

        return new PageImpl<>(pagedList, pageable, wishListDtoList.size());
    }

    public void deleteWishList(Long bookId, Long memberId) {
        if (memberRepository.findById(memberId).isEmpty()) {
            throw new ApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (bookRepository.findById(bookId).isEmpty()) {
            throw new ApplicationException(ErrorCode.BOOK_NOT_FOUND);
        }

        bookLikeService.unlikeBook(bookId, memberId);
    }

    public List<Book> getWishListByMemberId(Long memberId) {
        return bookLikeService.getWishListByMemberId(memberId);
    }
}

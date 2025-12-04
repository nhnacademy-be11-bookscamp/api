package store.bookscamp.api.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import store.bookscamp.api.book.controller.request.BookUpdateRequest;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookDocument;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.book.service.dto.BookCreateDto;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.book.service.dto.BookIndexDto;
import store.bookscamp.api.book.service.dto.BookSortDto;
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
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.member.repository.MemberRepository;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.repository.TagRepository;

@SpringBootTest
@ActiveProfiles("test")
class BookServiceTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final BookCategoryRepository bookCategoryRepository = mock(BookCategoryRepository.class);
    private final BookTagRepository bookTagRepository = mock(BookTagRepository.class);
    private final BookImageRepository bookImageRepository = mock(BookImageRepository.class);
    private final BookImageService bookImageService = mock(BookImageService.class);
    private final BookIndexService bookIndexService = mock(BookIndexService.class);
    private final BookLikeService bookLikeService = mock(BookLikeService.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final BookCachingIndexService bookCachingIndexService = mock(BookCachingIndexService.class);

    private BookService createService() {
        return new BookService(
                bookRepository,
                categoryRepository,
                tagRepository,
                bookCategoryRepository,
                bookTagRepository,
                bookImageRepository,
                bookImageService,
                bookIndexService,
                bookLikeService,
                memberRepository,
                bookCachingIndexService
        );
    }

    @Test
    @DisplayName("createBook - 전체 성공 플로우")
    void createBook_success() {

        BookService service = createService();

        BookCreateDto dto = new BookCreateDto(
                "title",
                "contrib",
                "pub",
                "isbn",
                LocalDate.of(2023, 1, 1),
                1000,
                900,
                10,
                true,
                "content",
                "exp",
                new ArrayList<>(Arrays.asList("img1", "img2")),
                new ArrayList<>(Arrays.asList(10L, 20L)),
        1L

        );

        Category mockCategory = Mockito.mock(Category.class);
        when(categoryRepository.getCategoryById(1L)).thenReturn(mockCategory);

        Tag t1 = new Tag("A");
        Tag t2 = new Tag("B");
        when(tagRepository.getTagById(10L)).thenReturn(t1);
        when(tagRepository.getTagById(20L)).thenReturn(t2);

        Book saved = new Book("title", "exp", "content", "pub",
                LocalDate.now(), "isbn", "con", BookStatus.AVAILABLE, true, 1000, 900, 10, 0);
        when(bookRepository.saveAndFlush(any())).thenReturn(saved);

        BookDocument mockDoc = new BookDocument();
        when(bookIndexService.mapBookToDocument(any(Book.class))).thenReturn(mockDoc);

        service.createBook(dto);

        verify(bookRepository, times(1)).saveAndFlush(any());
        verify(bookImageService, times(1)).createBookImage(any(BookImageCreateDto.class));
        verify(bookCategoryRepository, times(1)).save(any(BookCategory.class));
        verify(bookIndexService, times(1)).indexBook(any(BookDocument.class));
        verify(bookTagRepository, times(2)).save(any(BookTag.class));
    }

    @Test
    @DisplayName("updateBook - 전체 성공 플로우")
    void updateBook_success() {

        BookService service = createService();

        Book book = new Book(
                "old", "exp", "content",
                "pub", LocalDate.of(2023,1,1),
                "isbn","c", BookStatus.AVAILABLE,
                true, 1000, 900, 10, 0
        );

        try {
            var f = Book.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(book, 10L);
        } catch (Exception ignored) {}

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        BookUpdateRequest req = new BookUpdateRequest(
                "newTitle","newCon","newPub",
                "newIsbn", LocalDate.of(2024,1,1),
                2000, 1500, 5, true,
                "newContent","newExp",
                List.of(1L,2L),
                1L,
                List.of("newImg1","newImg2"),
                List.of("oldImg"),
                BookStatus.SOLD_OUT
                );

        Category cat = mock(Category.class);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

        BookProjection prj = mock(BookProjection.class);
        when(bookRepository.findByIdWithRatingAndReview(10L)).thenReturn(prj);

        BookDocument mockDoc = new BookDocument();
        when(bookIndexService.projectionToDoc(any(BookProjection.class))).thenReturn(mockDoc);

        Tag tg1 = new Tag("T1");
        Tag tg2 = new Tag("T2");
        when(tagRepository.getTagById(1L)).thenReturn(tg1);
        when(tagRepository.getTagById(2L)).thenReturn(tg2);

        BookImage img = mock(BookImage.class);
        when(bookImageRepository.findByImageUrl("oldImg")).thenReturn(Optional.of(img));
        when(img.getId()).thenReturn(99L);
        when(img.getImageUrl()).thenReturn("oldImg");

        service.updateBook(10L, req);

        verify(bookImageService, times(1))
                .deleteBookImage(any(BookImageDeleteDto.class));
        verify(bookImageService, times(1))
                .createBookImage(any(BookImageCreateDto.class));
        verify(bookCategoryRepository, times(1)).deleteByBook(book);
        verify(bookTagRepository, times(1)).deleteByBook(book);
        verify(bookIndexService, times(1)).indexBook(mockDoc);
        verify(bookCachingIndexService, times(1))
                .invalidateCachesContainingBook(10L);
    }


    @Test
    @DisplayName("deleteBook - 소프트 삭제 + 관련 엔티티 softDelete")
    void deleteBook_success() {

        BookService service = createService();

        Book book = new Book(
                "t", "e", "c",
                "p", LocalDate.now(),
                "isbn", "con", BookStatus.AVAILABLE,
                true, 1000, 900, 10, 0
        );

        try {
            var f = Book.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(book, 88L);
        } catch (Exception ignored) {
        }

        when(bookRepository.findById(88L)).thenReturn(Optional.of(book));

        BookTag t1 = mock(BookTag.class);
        BookTag t2 = mock(BookTag.class);
        when(bookTagRepository.findAllByBookId(88L)).thenReturn(List.of(t1, t2));

        BookCategory c1 = mock(BookCategory.class);
        BookCategory c2 = mock(BookCategory.class);
        when(bookCategoryRepository.findAllByBookId(88L)).thenReturn(List.of(c1, c2));

        service.deleteBook(88L);

        verify(bookIndexService).deleteBookIndex(88L);
        verify(bookCachingIndexService).invalidateCachesContainingBook(88L);
        verify(t1).softDelete();
        verify(t2).softDelete();
        verify(c1).softDelete();
        verify(c2).softDelete();
    }

    @Test
    @DisplayName("getBookDetail - 조회 + 카테고리/태그/이미지 조립")
    void getBookDetail_success() {

        BookService service = createService();

        Book book = new Book(
                "t","e","c","p",
                LocalDate.of(2023,1,1),
                "isbn","con",
                BookStatus.AVAILABLE,true,
                1000,900,10,0
        );

        try {
            var f = Book.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(book, 5L);
        } catch (Exception ignored) {}

        when(bookRepository.getBookById(5L)).thenReturn(book);

        BookProjection prj = mock(BookProjection.class);
        when(bookRepository.findByIdWithRatingAndReview(5L)).thenReturn(prj);

        BookDocument mockDoc = new BookDocument();
        when(bookIndexService.projectionToDoc(any(BookProjection.class))).thenReturn(mockDoc);

        Category c = mock(Category.class);
        when(c.getId()).thenReturn(1L);
        when(c.getName()).thenReturn("문학");
        List<BookCategory> catList = List.of(new BookCategory(book, c));
        when(bookCategoryRepository.findByBook_Id(5L)).thenReturn(catList);

        Tag t = new Tag("Hi");
        List<BookTag> tagList = List.of(new BookTag(book, t));
        when(bookTagRepository.findByBook_Id(5L)).thenReturn(tagList);

        BookImage img = mock(BookImage.class);
        when(img.getImageUrl()).thenReturn("A.jpg");
        when(bookImageRepository.findByBook_Id(5L))
                .thenReturn(List.of(img));

        BookDetailDto dto = service.getBookDetail(5L);

        assertThat(dto.title()).isEqualTo("t");
        assertThat(dto.imageUrlList()).contains("A.jpg");
        verify(bookIndexService).indexBook(mockDoc);
    }


    @Test
    @DisplayName("getRecommendBooks - thumbnail 포함 반환")
    void recommendBooks_ok() {

        BookService service = createService();

        Book b = mock(Book.class);
        when(b.getId()).thenReturn(10L);
        when(b.getTitle()).thenReturn("TT");
        when(b.getPublisher()).thenReturn("PUB");
        when(b.getContributors()).thenReturn("CON");
        when(b.getRegularPrice()).thenReturn(1000);
        when(b.getSalePrice()).thenReturn(800);

        when(bookRepository.getRecommendBooks()).thenReturn(List.of(b));

        BookImage img = mock(BookImage.class);
        when(img.isThumbnail()).thenReturn(true);
        when(img.getImageUrl()).thenReturn("THUMB.jpg");
        when(bookImageRepository.findByBook(b)).thenReturn(List.of(img));

        List<BookIndexDto> result = service.getRecommendBooks();

        assertThat(result.get(0).thumbnail()).isEqualTo("THUMB.jpg");
    }

    @Test
    @DisplayName("getWishList - 페이징 포함 반환")
    void wishList_ok() {

        BookService service = createService();

        Book b1 = mock(Book.class);
        when(b1.getId()).thenReturn(1L);
        when(b1.getTitle()).thenReturn("A");
        when(b1.getPublishDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(b1.getPublisher()).thenReturn("P");
        when(b1.getContributors()).thenReturn("C");
        when(b1.getRegularPrice()).thenReturn(1000);
        when(b1.getSalePrice()).thenReturn(800);
        when(b1.getStatus()).thenReturn(BookStatus.AVAILABLE);
        when(b1.isPackable()).thenReturn(true);

        Book b2 = mock(Book.class);
        when(b2.getId()).thenReturn(2L);
        when(b2.getTitle()).thenReturn("B");
        when(b2.getPublishDate()).thenReturn(LocalDate.of(2023, 1, 2));
        when(b2.getPublisher()).thenReturn("P2");
        when(b2.getContributors()).thenReturn("C2");
        when(b2.getRegularPrice()).thenReturn(2000);
        when(b2.getSalePrice()).thenReturn(1500);
        when(b2.getStatus()).thenReturn(BookStatus.AVAILABLE);
        when(b2.isPackable()).thenReturn(true);

        when(bookLikeService.getWishListByMemberId(99L))
                .thenReturn(List.of(b1, b2));

        when(bookImageService.getThumbnailUrl(1L)).thenReturn("A.jpg");
        when(bookImageService.getThumbnailUrl(2L)).thenReturn("B.jpg");

        Pageable pageable = PageRequest.of(0, 1);

        Page<BookWishListDto> result = service.getWishList(99L, pageable);

        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).thumbnailUrl()).isEqualTo("A.jpg");
    }

    @Test
    @DisplayName("deleteWishList - member/book 존재하지 않으면 예외")
    void deleteWishList_fail() {

        BookService service = createService();

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWishList(10L, 1L))
                .isInstanceOf(ApplicationException.class);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(Mockito.mock(Member.class)));

        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWishList(10L, 1L))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("deleteWishList - 정상 unlike 호출됨")
    void deleteWishList_ok() {

        BookService service = createService();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(Mockito.mock(Member.class)));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(Mockito.mock(Book.class)));

        service.deleteWishList(10L, 1L);

        verify(bookLikeService).unlikeBook(10L, 1L);
    }

    @Test
    @DisplayName("BookSortDto.from(Book) - Book 엔티티 기반 DTO 변환 검증")
    void dto_fromBook_success() {

        Book book = new Book(
                "제목", "설명", "콘텐츠",
                "출판사", LocalDate.of(2024, 1, 1),
                "1234567890123", "기여자",
                BookStatus.AVAILABLE, true,
                20000, 15000, 10, 5
        );

        BookSortDto dto = BookSortDto.from(book);

        assertThat(dto.getTitle()).isEqualTo("제목");
        assertThat(dto.getContributors()).isEqualTo("기여자");
        assertThat(dto.getPublisher()).isEqualTo("출판사");
        assertThat(dto.getPublishDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(dto.getRegularPrice()).isEqualTo(20000);
        assertThat(dto.getSalePrice()).isEqualTo(15000);
        assertThat(dto.getStock()).isEqualTo(10);
        assertThat(dto.getViewCount()).isEqualTo(5);
        assertThat(dto.isPackable()).isTrue();

        // 기본값 검증
        assertThat(dto.getIsbn()).isNull();
        assertThat(dto.getAverageRating()).isEqualTo(0.0);
        assertThat(dto.getReviewCount()).isEqualTo(0L);
        assertThat(dto.getAiRecommand()).isEqualTo("");
        assertThat(dto.getAiRank()).isEqualTo(0);
    }

    @Test
    @DisplayName("BookSortDto.fromDocument(BookDocument) - ES 문서 기반 DTO 변환 검증")
    void dto_fromDocument_success() {

        BookDocument doc = BookDocument.builder()
                .id(999L)
                .title("도큐먼트 책")
                .publisher("ES출판사")
                .publishDate(LocalDate.of(2023, 12, 25))
                .contributors("Doc Writer")
                .packable(false)
                .regularPrice(30000)
                .salePrice(25000)
                .stock(50)
                .viewCount(100L)
                .isbn("ABC123")
                .averageRating(4.7)
                .reviewCount(321L)
                .build();

        BookSortDto dto = BookSortDto.fromDocument(doc);

        assertThat(dto.getId()).isEqualTo(999L);
        assertThat(dto.getTitle()).isEqualTo("도큐먼트 책");
        assertThat(dto.getPublisher()).isEqualTo("ES출판사");
        assertThat(dto.getPublishDate()).isEqualTo(LocalDate.of(2023, 12, 25));
        assertThat(dto.getContributors()).isEqualTo("Doc Writer");
        assertThat(dto.getSalePrice()).isEqualTo(25000);
        assertThat(dto.getStock()).isEqualTo(50);
        assertThat(dto.getViewCount()).isEqualTo(100L);
        assertThat(dto.getIsbn()).isEqualTo("ABC123");
        assertThat(dto.getAverageRating()).isEqualTo(4.7);
        assertThat(dto.getReviewCount()).isEqualTo(321L);
        assertThat(dto.isPackable()).isFalse();

        // 기본값 검증
        assertThat(dto.getAiRecommand()).isEqualTo("");
        assertThat(dto.getAiRank()).isEqualTo(0);
    }

}

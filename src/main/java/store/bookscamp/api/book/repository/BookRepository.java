package store.bookscamp.api.book.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.book.entity.Book;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.book.service.dto.BookSortDto;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 제목순 정렬
    List<Book> findAllByOrderByTitleAsc();

    // 출판일자순 정렬
    List<Book> findAllByOrderByPublishDateDesc();

    // 낮은금액순 정렬
    List<Book> findAllByOrderBySalePriceAsc();

    // 높은금액순 정렬
    List<Book> findAllByOrderBySalePriceDesc();

    // 조회많은순 정렬
    List<Book> findAllByOrderByViewCountDesc();

    // 리뷰많은순 정렬
    @Query("SELECT new store.bookscamp.api.book.service.dto.BookSortDto(b.id, b.title, b.publisher, COUNT(r)) FROM Book b " +
            "LEFT JOIN OrderItem oi ON oi.book = b " +
            "LEFT JOIN Review r ON r.orderItem = oi " +
            "GROUP BY b.id " +
            "ORDER BY COUNT(r) DESC"
    )
    List<BookSortDto> findAllOrderByReviewCountDesc();

    // 평점높은순
    @Query("SELECT new store.bookscamp.api.book.service.dto.BookSortDto(b.id, b.title, b.publisher, COALESCE(AVG(r.score), 0.0)) " +
            "FROM Book b " +
            "LEFT JOIN OrderItem oi ON oi.book = b " +
            "LEFT JOIN Review r ON r.orderItem = oi " +
            "GROUP BY b.id " +
            "ORDER BY COALESCE(AVG(r.score), 0.0) DESC"
    )
    List<BookSortDto> findAllByOrderByScoreDesc();

}

package store.bookscamp.api.book.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    Book getBookById(Long id);

    @Query(value = """
    SELECT a.id AS id,
           a.title AS title,
           a.explanation AS explanation,
           a.content AS content,
           a.publisher AS publisher,
           a.publish_date AS publishDate,
           a.isbn AS isbn,
           a.contributors AS contributors,
           a.regular_price AS regularPrice,
           a.sale_price AS salePrice,
           a.stock AS stock,
           a.view_count AS viewCount,
           a.packable AS packable,
           a.status AS status,
           COALESCE(b.average_rating, 0) AS averageRating,
           COALESCE(b.review_count, 0) AS reviewCount,
           c.category_name AS category   -- ✅ 카테고리 이름
    FROM book a
    LEFT JOIN (
        SELECT oi.book_id,
               COUNT(r.id) AS review_count,
               AVG(r.score) AS average_rating
        FROM review r
        JOIN order_item oi ON r.order_item_id = oi.id
        GROUP BY oi.book_id
    ) b ON a.id = b.book_id
    LEFT JOIN (
        SELECT bc.book_id,
               GROUP_CONCAT(cat.name ORDER BY cat.id SEPARATOR ',') AS category_name
        FROM book_category bc
        JOIN category cat ON bc.category_id = cat.id
        WHERE bc.deleted_at IS NULL
        GROUP BY bc.book_id
    ) c ON a.id = c.book_id
    WHERE a.deleted_at IS NULL           -- ✅ 삭제되지 않은 도서만
""", nativeQuery = true)
    List<BookProjection> findAllBooksWithRatingAndReview();




    @Query(value = """
    SELECT a.id AS id,
           a.title AS title,
           a.explanation AS explanation,
           a.content AS content,
           a.publisher AS publisher,
           a.publish_date AS publishDate,
           a.isbn AS isbn,
           a.contributors AS contributors,
           a.regular_price AS regularPrice,
           a.sale_price AS salePrice,
           a.stock AS stock,
           a.view_count AS viewCount,
           a.packable AS packable,
           a.status AS status,
           COALESCE(b.average_rating, 0) AS averageRating,
           COALESCE(b.review_count, 0) AS reviewCount,
           c.category_name AS category   -- ✅ 카테고리 이름
    FROM book a
    LEFT JOIN (
        SELECT oi.book_id,
               COUNT(r.id) AS review_count,
               AVG(r.score) AS average_rating
        FROM review r
        JOIN order_item oi ON r.order_item_id = oi.id
        GROUP BY oi.book_id
    ) b ON a.id = b.book_id
    LEFT JOIN (
        SELECT bc.book_id,
               GROUP_CONCAT(cat.name ORDER BY cat.id SEPARATOR ',') AS category_name
        FROM book_category bc
        JOIN category cat ON bc.category_id = cat.id
        WHERE bc.deleted_at IS NULL
        GROUP BY bc.book_id
    ) c ON a.id = c.book_id
    WHERE a.deleted_at IS NULL  
    and a.id= :bookId
""", nativeQuery = true)
    BookProjection findByIdWithRatingAndReview(@Param("bookId")Long id);


}

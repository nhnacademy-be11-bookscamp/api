package store.bookscamp.api.book.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookProjection;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    Book getBookById(Long id);

    @Query(value = """
                    SELECT\s
                        a.id AS id,\s
                        a.title AS title,\s
                        a.explanation AS explanation,
                        a.content AS content,\s
                        a.publisher AS publisher,\s
                        a.publish_date AS publishDate,
                        a.isbn AS isbn,\s
                        a.contributors AS contributors,
                        a.regular_price AS regularPrice,\s
                        a.sale_price AS salePrice,
                        a.stock AS stock,\s
                        a.view_count AS viewCount,\s
                        a.packable AS packable,
                        a.status AS status,
                        COALESCE(b.average_rating, 0) AS averageRating,
                        COALESCE(b.review_count, 0) AS reviewCount,
                        COALESCE(GROUP_CONCAT(c.name SEPARATOR ', '), '') AS category
                    FROM book a
                    LEFT JOIN (
                        SELECT\s
                            oi.book_id,
                            COUNT(r.id) AS review_count,
                            AVG(r.score) AS average_rating
                        FROM review r
                        JOIN order_item oi ON r.order_item_id = oi.id
                        GROUP BY oi.book_id
                    ) b ON a.id = b.book_id
                    LEFT JOIN (
                        SELECT\s
                            bc.book_id,
                            c.name
                        FROM book_category bc
                        JOIN category c ON bc.category_id = c.id
                    ) c ON a.id = c.book_id
                    GROUP BY\s
                        a.id, a.title, a.explanation, a.content, a.publisher,\s
                        a.publish_date, a.isbn, a.contributors, a.regular_price,\s
                        a.sale_price, a.stock, a.view_count, a.packable, a.status,\s
                        b.average_rating, b.review_count
                """, nativeQuery = true)
    List<BookProjection> findAllBooksWithRatingAndReview();


}

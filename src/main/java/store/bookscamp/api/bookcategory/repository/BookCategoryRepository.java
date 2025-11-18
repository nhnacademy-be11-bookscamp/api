package store.bookscamp.api.bookcategory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.bookcategory.entity.BookCategory;
import store.bookscamp.api.category.entity.Category;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {

    @Query("SELECT bc FROM BookCategory bc WHERE bc.book.id = :bookId")
    List<BookCategory> findAllByBookId(@Param("bookId") Long id);

    void deleteByBook(Book book);

    boolean existsByBookAndCategory(Book book, Category category);

    List<BookCategory> findByBook_Id(Long bookId);

    boolean existsByBookIdAndCategoryId(Long bookId, Long targetId);
}

package store.bookscamp.api.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.review.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderItemAndMember(OrderItem orderItem, Member member);

    Review findByOrderItemAndMember(OrderItem orderItem, Member member);

    Page<Review> findByOrderItemBookId(Long bookId, Pageable pageable);

    @Query("select avg(r.score) from Review r where r.orderItem.book.id = :bookId")
    Double getAvgScore(Long bookId);

    @Query("SELECT r FROM Review r " +
            "JOIN r.orderItem oi " +
            "JOIN oi.book b " +
            "WHERE b.id = :bookId AND LENGTH(r.content) >= 50 " +
            "ORDER BY r.score DESC, r.createdAt DESC")
    List<Review> findAiReviewsByBookId(Long bookId);
}

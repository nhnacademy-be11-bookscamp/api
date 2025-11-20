package store.bookscamp.api.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderItemAndMember(OrderItem orderItem, Member member);

    Page<Review> findByOrderItemBookId(Long bookId, Pageable pageable);

    @Query("select avg(r.score) from Review r where r.orderItem.book.id = :bookId")
    Double getAvgScore(Long bookId);
}

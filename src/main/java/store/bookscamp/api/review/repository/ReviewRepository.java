package store.bookscamp.api.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.member.entity.Member;
import store.bookscamp.api.orderitem.entity.OrderItem;
import store.bookscamp.api.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderItemAndMember(Member member, OrderItem orderItem);
}

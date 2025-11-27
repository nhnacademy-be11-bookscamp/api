package store.bookscamp.api.nonmember.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.nonmember.entity.NonMember;

public interface NonMemberRepository extends JpaRepository<NonMember, Long> {
    Optional<NonMember> findByOrderInfo_OrderNumber(String orderNumber);
}

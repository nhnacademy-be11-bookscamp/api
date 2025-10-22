package store.bookscamp.api.nonmember.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.nonmember.entity.NonMember;

public interface NonMemberRepository extends JpaRepository<NonMember, Long> {
}

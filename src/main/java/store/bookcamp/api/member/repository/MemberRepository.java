package store.bookcamp.api.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookcamp.api.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member getByAccountId(String id);
}

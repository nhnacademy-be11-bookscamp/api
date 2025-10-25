package store.bookscamp.api.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> getByAccountId(String id);

    boolean existsByAccountId(String id);
}

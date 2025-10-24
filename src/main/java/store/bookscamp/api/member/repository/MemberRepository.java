package store.bookscamp.api.member.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member getByAccountId(String id);

    boolean existsByAccountId(String id);

    List<Member> getMemberById(Long id);
}

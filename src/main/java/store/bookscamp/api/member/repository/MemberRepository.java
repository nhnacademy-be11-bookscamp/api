package store.bookscamp.api.member.repository;

<<<<<<< HEAD
import java.util.List;
=======
import java.util.Optional;
>>>>>>> 911d5544d059a2a960555c01f8d3cc66b384515d
import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> getByAccountId(String id);

    boolean existsByAccountId(String id);

    List<Member> getMemberById(Long id);
}

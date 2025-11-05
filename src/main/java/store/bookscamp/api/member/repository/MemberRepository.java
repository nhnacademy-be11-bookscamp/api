package store.bookscamp.api.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> getByUsername(String id);

    boolean existsByUsername(String id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("select m from Member m where MONTH(m.birthDate) = :month")
    List<Member> findAllByBirthDateMonth(int month);
}

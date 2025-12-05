package store.bookscamp.api.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(Long id);

    @Query(
        value = "SELECT COUNT(*) > 0 FROM member m WHERE m.username = :username", 
        nativeQuery = true
    )
    boolean existsByUsername(@Param("username")String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("select m from Member m where MONTH(m.birthDate) = :month")
    List<Member> findAllByBirthDateMonth(int month);

    Page<Member> findAll(Pageable pageable);

}

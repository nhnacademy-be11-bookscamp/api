package store.bookscamp.api.address.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.address.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
            SELECT a
            FROM Address a
            WHERE a.member.id = :memberId
            """)
    List<Address> findAllByMemberId(@Param("memberId") Long memberId);

    long countByMemberId(Long memberId);

    @Query("""
            SELECT a
            FROM Address a
            WHERE a.id = :addressId
              AND a.member.id = :memberId
            """)
    Optional<Address> findByIdAndMemberId(
            @Param("addressId") Long addressId,
            @Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Address a
            SET a.isDefault = false
            WHERE a.member.id = :memberId
              AND a.isDefault = true
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    int clearDefaultForMember(@Param("memberId") Long memberId,
                              @Param("excludeId") Long excludeId);

}

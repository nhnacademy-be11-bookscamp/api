package store.bookscamp.api.address.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.bookscamp.api.address.entity.Address;
import store.bookscamp.api.member.entity.Member;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
            SELECT a 
            FROM Address a 
            JOIN a.member m 
            WHERE m.username = :username
            """)
    List<Address> getAllByMemberUserId(@Param("username") String username);

    @Query("""
            SELECT COUNT(a)
            FROM Address a
            JOIN a.member m
            WHERE m.username = :username
            """)
    long countByMemberUsername(@Param("username") String username);


    @Query("""
            SELECT a 
            FROM Address a 
            JOIN a.member m 
            WHERE a.id = :addressId 
            AND m.username = :username
            """)
    Optional<Address> getByIdAndMemberUserId(
            @Param("addressId") Long addressId,
            @Param("username") String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Address a
            SET a.isDefault = false
            WHERE a.member = :member
              AND a.isDefault = true
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    int clearDefaultForMember(@Param("member") Member member,
                              @Param("excludeId") Long excludeId);

}

package store.bookscamp.api.address.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.address.entity.Address;

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
    Optional<Address> getByIdAndMemberUserId(Integer addressId, String username);
}

package store.bookscamp.api.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.address.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
}

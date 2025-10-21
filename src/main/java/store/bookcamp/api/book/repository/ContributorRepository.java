package store.bookcamp.api.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookcamp.api.book.entity.Book;
import store.bookcamp.api.book.entity.Contributor;

public interface ContributorRepository extends JpaRepository<Contributor, Long> {
    
}

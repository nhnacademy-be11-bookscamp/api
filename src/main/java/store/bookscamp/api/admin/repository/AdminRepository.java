package store.bookscamp.api.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.bookscamp.api.admin.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}

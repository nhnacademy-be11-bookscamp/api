package store.bookscamp.api.book.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import store.bookscamp.api.book.entity.Book;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.book.repository.custom.BookRepositoryCustom;
import store.bookscamp.api.book.service.dto.BookSortDto;

import store.bookscamp.api.admin.entity.QAdmin;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

}

package store.bookscamp.api.category.repository;

import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.bookscamp.api.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = """
        WITH RECURSIVE CategoryTree AS (
            SELECT id
            FROM category                
            WHERE id = :categoryId
            
            UNION ALL
            
            SELECT c.id
            FROM category c              
            INNER JOIN CategoryTree ct ON c.parent_id = ct.id
        )
        SELECT id FROM CategoryTree
        """, nativeQuery = true)
    List<Long> getAllDescendantIdsIncludingSelf(@Param("categoryId") Long categoryId);
}

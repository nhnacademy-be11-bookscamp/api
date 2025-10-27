package store.bookscamp.api.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import store.bookscamp.api.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 현재 카테고리 ID를 포함한 모든 하위 카테고리 ID를 재귀적으로 조회합니다.
     * (PostgreSQL, MySQL 8.0+, MariaDB 10.2+ 표준 SQL 기준)
     */
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
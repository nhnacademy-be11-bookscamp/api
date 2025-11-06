package store.bookscamp.api.category.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import store.bookscamp.api.category.entity.Category;
import store.bookscamp.api.common.config.QueryDslConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DataJpaTest
@Import(QueryDslConfig.class)
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("getAllDescendantIdsIncludingSelf - 하위 카테고리 ID 목록 재귀 조회")
    void getAllDescendantIdsIncludingSelf_Success() {
        // given
        Category root = new Category(null, "Root");
        entityManager.persist(root);

        Category child1 = new Category(root, "Child 1");
        entityManager.persist(child1);

        Category child2 = new Category(root, "Child 2");
        entityManager.persist(child2);

        Category grandChild1 = new Category(child1, "Grandchild 1.1");
        entityManager.persist(grandChild1);

        Category otherRoot = new Category(null, "Other Root");
        entityManager.persist(otherRoot);

        entityManager.flush();

        // when
        List<Long> resultFromRoot = categoryRepository.getAllDescendantIdsIncludingSelf(root.getId());

        List<Long> resultFromChild1 = categoryRepository.getAllDescendantIdsIncludingSelf(child1.getId());

        List<Long> resultFromLeaf = categoryRepository.getAllDescendantIdsIncludingSelf(grandChild1.getId());

        List<Long> resultFromOther = categoryRepository.getAllDescendantIdsIncludingSelf(otherRoot.getId());

        // then
        assertThat(resultFromRoot)
                .hasSize(4)
                .containsExactlyInAnyOrder(root.getId(), child1.getId(), child2.getId(), grandChild1.getId());

        assertThat(resultFromChild1)
                .hasSize(2)
                .containsExactlyInAnyOrder(child1.getId(), grandChild1.getId());

        assertThat(resultFromLeaf)
                .hasSize(1)
                .containsExactly(grandChild1.getId());

        assertThat(resultFromOther)
                .hasSize(1)
                .containsExactly(otherRoot.getId());
    }

    @Test
    @DisplayName("getCategoryById - ID로 카테고리 조회")
    void getCategoryById_Success() {
        // given
        Category category = new Category(null, "Test Category");
        entityManager.persist(category);
        entityManager.flush();

        // when
        Category found = categoryRepository.getCategoryById(category.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(category.getId());
        assertThat(found.getName()).isEqualTo("Test Category");
    }

    @Test
    @DisplayName("existsByNameAndParent - 이름과 부모로 존재 여부 확인")
    void existsByNameAndParent_Success() {
        // given
        Category root = new Category(null, "Root");
        entityManager.persist(root);

        Category child = new Category(root, "Child");
        entityManager.persist(child);

        entityManager.flush();

        // when
        boolean rootExists = categoryRepository.existsByNameAndParent("Root", null);
        boolean rootNotExists = categoryRepository.existsByNameAndParent("Wrong Root", null);

        boolean childExists = categoryRepository.existsByNameAndParent("Child", root);
        boolean childNotExists = categoryRepository.existsByNameAndParent("Wrong Child", root);
        boolean childButWrongParent = categoryRepository.existsByNameAndParent("Child", null); // 이름은 같지만 부모가 다름

        // then
        assertThat(rootExists).isTrue();
        assertThat(rootNotExists).isFalse();

        assertThat(childExists).isTrue();
        assertThat(childNotExists).isFalse();
        assertThat(childButWrongParent).isFalse();
    }
}
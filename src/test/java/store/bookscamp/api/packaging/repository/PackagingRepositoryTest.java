package store.bookscamp.api.packaging.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import store.bookscamp.api.packaging.entity.Packaging;

@DataJpaTest
@EntityScan(basePackages = "store.bookscamp.api.packaging.entity")
@EnableJpaRepositories(basePackages = "store.bookscamp.api.packaging.repository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PackagingRepositoryTest {

    @Autowired
    PackagingRepository packagingRepository;

    @Test
    @DisplayName("existsByName - 저장된 이름이면 true/아니면 false")
    void existsByName() {
        packagingRepository.save(new Packaging("포장A", 1000, "u1"));
        assertThat(packagingRepository.existsByName("포장A")).isTrue();
        assertThat(packagingRepository.existsByName("없는이름")).isFalse();
    }

    @Test
    @DisplayName("findByName - 이름으로 조회")
    void findByName() {
        packagingRepository.save(new Packaging("B", 2000, "u2"));
        Optional<Packaging> found = packagingRepository.findByName("B");
        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualTo(2000);
        assertThat(found.get().getImageUrl()).isEqualTo("u2");
    }
}

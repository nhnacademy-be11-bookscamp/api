package store.bookscamp.api.packaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.tuple;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.packaging.controller.request.PackagingCreateRequest;
import store.bookscamp.api.packaging.controller.request.PackagingUpdateRequest;
import store.bookscamp.api.packaging.controller.response.PackagingGetResponse;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;

@SpringBootTest
@Transactional
class PackagingServiceTest {

    // 실제 PackagingService 빈을 주입받아 테스트합니다.
    @Autowired
    private PackagingService packagingService;

    @Autowired
    private PackagingRepository packagingRepository;

    private Packaging createPackaging(String name, int price, String imageUrl) {
        return packagingRepository.save(new Packaging(name, price, imageUrl));
    }

    @DisplayName("새로운 포장지를 등록한다.")
    @Test
    void createPackagingTest() {
        // given
        PackagingCreateRequest request = new PackagingCreateRequest("선물 포장 A", 3000, List.of("url_a_1.jpg"));

        // when
        PackagingGetResponse response = packagingService.create(request);

        // then
        assertThat(response.getName()).isEqualTo("선물 포장 A");
        assertThat(response.getPrice()).isEqualTo(3000);
        assertThat(response.getImageUrl()).isEqualTo("url_a_1.jpg");

        List<Packaging> packagings = packagingRepository.findAll();
        assertThat(packagings).hasSize(1)
                .extracting("name", "price", "imageUrl")
                .containsExactly(
                        // Tuple.tuple()을 사용하여 예상되는 한 객체의 필드 묶음을 전달합니다.
                        // 순서는 extracting의 순서("name", "price", "imageUrl")와 일치해야 합니다.
                        tuple(response.getName(), response.getPrice(), response.getImageUrl())
                );
    }

    @DisplayName("이미 존재하는 이름으로 포장지를 등록하면 예외가 발생한다.")
    @Test
    void createPackagingWithDuplicateNameTest() {
        // given
        createPackaging("선물 포장 B", 5000, "url_b.jpg");
        PackagingCreateRequest request = new PackagingCreateRequest("선물 포장 B", 6000, List.of("new_url.jpg"));

        // when // then
        assertThatThrownBy(() -> packagingService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PACKAGING_DUPLICATE_RESOURCE);
    }

    // --- Read (Get All) Test ---

    @DisplayName("전체 포장지 목록을 조회한다.")
    @Test
    void getAllPackagingsTest() {
        // given
        Packaging packaging1 = createPackaging("선물 포장 C", 1000, "url_c.jpg");
        Packaging packaging2 = createPackaging("선물 포장 D", 2000, "url_d.jpg");

        // when
        List<PackagingGetResponse> responses = packagingService.getAll();

        // then
        assertThat(responses).hasSize(2)
                .extracting("name", "price")
                .containsExactlyInAnyOrder(
                        // 첫 번째 포장지의 예상 필드 묶음
                        tuple(packaging1.getName(), packaging1.getPrice()),
                        // 두 번째 포장지의 예상 필드 묶음
                        tuple(packaging2.getName(), packaging2.getPrice())
                );
    }

    // --- Read (Get by ID) Test ---

    @DisplayName("ID로 포장지를 단건 조회한다.")
    @Test
    void getPackagingByIdTest() {
        // given
        Packaging packaging = createPackaging("선물 포장 E", 4000, "url_e.jpg");

        // when
        PackagingGetResponse response = packagingService.get(packaging.getId());

        // then
        assertThat(response.getName()).isEqualTo("선물 포장 E");
        assertThat(response.getPrice()).isEqualTo(4000);
    }

    @DisplayName("존재하지 않는 ID로 포장지를 조회하면 예외가 발생한다.")
    @Test
    void getPackagingByNonExistentIdTest() {
        // given
        Long nonExistentId = 999L;

        // when // then
        assertThatThrownBy(() -> packagingService.get(nonExistentId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PACKAGING_NOT_FOUND);
    }

    // --- Update Test ---

    @DisplayName("포장지의 이름, 가격, 이미지를 모두 수정한다.")
    @Test
    void updateAllFieldsTest() {
        // given
        Packaging packaging = createPackaging("구 포장지", 1000, "old_url.jpg");
        PackagingUpdateRequest request = new PackagingUpdateRequest("신규 포장지", 5000, List.of("new_url.jpg"));

        // when
        PackagingGetResponse response = packagingService.update(packaging.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("신규 포장지");
        assertThat(response.getPrice()).isEqualTo(5000);
        assertThat(response.getImageUrl()).isEqualTo("new_url.jpg");

        // DB에서 실제로 변경되었는지 검증
        Packaging foundPackaging = packagingRepository.findById(packaging.getId()).get();
        assertThat(foundPackaging.getName()).isEqualTo("신규 포장지");
        assertThat(foundPackaging.getPrice()).isEqualTo(5000);
        assertThat(foundPackaging.getImageUrl()).isEqualTo("new_url.jpg");
    }

    @DisplayName("이미지 없이 이름과 가격만 수정한다. (hasImagePatch=false)")
    @Test
    void updateWithoutImageTest() {
        // given
        Packaging packaging = createPackaging("이름만 수정", 2000, "original_url.jpg");
        PackagingUpdateRequest request = new PackagingUpdateRequest("수정된 이름", 3000, null); // hasImagePatch: false

        // when
        PackagingGetResponse response = packagingService.update(packaging.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("수정된 이름");
        assertThat(response.getPrice()).isEqualTo(3000);
        assertThat(response.getImageUrl()).isEqualTo("original_url.jpg"); // 이미지는 변경되지 않아야 함
    }

    @DisplayName("이미지 포함 이름을 중복된 이름으로 수정 시도하면 예외가 발생한다. (다른 ID)")
    @Test
    void updateWithDuplicateNameTest() {
        // given
        createPackaging("중복 이름", 100, "url_dup.jpg"); // 이미 존재하는 포장지
        Packaging targetPackaging = createPackaging("타겟 포장지", 200, "url_target.jpg"); // 수정 대상 포장지

        // 중복 이름으로 수정 요청
        PackagingUpdateRequest request = new PackagingUpdateRequest("중복 이름", 300, List.of("new_url.jpg"));

        // when // then
        assertThatThrownBy(() -> packagingService.update(targetPackaging.getId(), request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PACKAGING_DUPLICATE_RESOURCE);
    }

    // --- Delete Test ---

    @DisplayName("ID로 포장지를 삭제한다.")
    @Test
    void deletePackagingTest() {
        // given
        Packaging packaging = createPackaging("삭제 대상", 7777, "url_delete.jpg");
        Long idToDelete = packaging.getId();

        // when
        packagingService.delete(idToDelete);

        // then
        // DB에서 해당 ID로 조회 시 결과가 없어야 한다.
        assertThat(packagingRepository.existsById(idToDelete)).isFalse();
    }

    @DisplayName("존재하지 않는 ID로 포장지를 삭제 시도하면 예외가 발생한다.")
    @Test
    void deleteNonExistentIdTest() {
        // given
        Long nonExistentId = 9999L;

        // when // then
        assertThatThrownBy(() -> packagingService.delete(nonExistentId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PACKAGING_NOT_FOUND);
    }
}
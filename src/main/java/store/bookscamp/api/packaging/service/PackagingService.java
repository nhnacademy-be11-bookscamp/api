package store.bookscamp.api.packaging.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.packaging.controller.request.PackagingCreateRequest;
import store.bookscamp.api.packaging.controller.request.PackagingUpdateRequest;
import store.bookscamp.api.packaging.controller.response.PackagingGetResponse;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PackagingService {

    private final PackagingRepository packagingRepository;

    @Transactional
    public PackagingGetResponse create(PackagingCreateRequest request) {
        if (packagingRepository.existsByName(request.getName())) {
            throw new ApplicationException(ErrorCode.PACKAGING_DUPLICATE_RESOURCE);
        }

        // List<String> imageUrls에서 첫번째만 사용 (포장지는 여러장의 사진을 등록할 필요가 없으니까)
        String imageUrl = request.primaryImageUrl();

        Packaging saved = packagingRepository.save(new Packaging(request.getName(), request.getPrice(), imageUrl));
        return PackagingGetResponse.from(saved);
    }

    public List<PackagingGetResponse> getAll() {
        return packagingRepository.findAll()
                .stream()
                .map(PackagingGetResponse::from)
                .toList();
    }

    public PackagingGetResponse get(Long id) {
        Packaging packaging = packagingRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PACKAGING_NOT_FOUND));
        return PackagingGetResponse.from(packaging);
    }

    @Transactional
    public PackagingGetResponse update(Long id, PackagingUpdateRequest request) {
        Packaging packaging = packagingRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PACKAGING_NOT_FOUND));

        packagingRepository.findByName(request.getName())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> { throw new ApplicationException(ErrorCode.PACKAGING_DUPLICATE_RESOURCE); });

        String newImageUrl = request.hasImagePatch() ? request.primaryImageUrlOrNull() : packaging.getImageUrl();

        // 이름, 가격, 이미지 동시 반영
        packaging.change(request.getName(), request.getPrice(), newImageUrl);
        return PackagingGetResponse.from(packaging);
    }

    @Transactional
    public void delete(Long id) {
        if (!packagingRepository.existsById(id)) {
            throw new ApplicationException(ErrorCode.PACKAGING_NOT_FOUND);
        }
        packagingRepository.deleteById(id);
    }
}

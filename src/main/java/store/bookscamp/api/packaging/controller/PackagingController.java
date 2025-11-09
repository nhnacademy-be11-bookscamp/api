package store.bookscamp.api.packaging.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.packaging.controller.request.PackagingCreateRequest;
import store.bookscamp.api.packaging.controller.request.PackagingUpdateRequest;
import store.bookscamp.api.packaging.controller.response.PackagingGetResponse;
import store.bookscamp.api.packaging.service.PackagingService;

@RestController
@RequiredArgsConstructor
@Tag(name = "[포장지] API", description = "Packaging API")
@RequestMapping("/admin/packagings")
public class PackagingController {

    private final PackagingService packagingService;

    // 관리자 : 셍성
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<String> createPackaging(@RequestBody @Valid PackagingCreateRequest request) {
        packagingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\": \"포장지 등록이 완료되었습니다.\"}");
    }

    // 관리자 : 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<PackagingGetResponse> getPackaging(@PathVariable Long id) {
        PackagingGetResponse response = packagingService.get(id);
        return ResponseEntity.ok(response);
    }

    // 관리자 : 전체 조회
    @GetMapping
    public ResponseEntity<List<PackagingGetResponse>> getAll() {
        List<PackagingGetResponse> responses = packagingService.getAll();
        return ResponseEntity.ok(responses);
    }

    // 관리자 : 수정
    @PutMapping(value = "/{id}/update", produces = "application/json")
    public ResponseEntity<String> updatePackaging(@PathVariable Long id,
                                       @RequestBody @Valid PackagingUpdateRequest request) {
        packagingService.update(id, request);
        return ResponseEntity.ok("{\"message\": \"포장지 정보가 수정되었습니다.\"}");
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deletePackaging(@PathVariable Long id) {
        packagingService.delete(id);
        return ResponseEntity.ok("{\"message\": \"포장지가 삭제되었습니다.\"}");
    }
}
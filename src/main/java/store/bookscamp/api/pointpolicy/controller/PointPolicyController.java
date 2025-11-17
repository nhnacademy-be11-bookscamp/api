package store.bookscamp.api.pointpolicy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.pointpolicy.controller.request.PointPolicyCreateRequest;
import store.bookscamp.api.pointpolicy.controller.request.PointPolicyUpdateRequest;
import store.bookscamp.api.pointpolicy.controller.response.PointPolicyResponse;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.service.PointPolicyService;

@RestController
@RequestMapping("/admin/point-policies")
@RequiredArgsConstructor
@Tag(name = "포인트정책 API", description = "PointPolicy API입니다.")
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    @PostMapping
    @RequiredRole("ADMIN")
    public ResponseEntity<Void> createPointPolicy(
            @RequestBody @Valid PointPolicyCreateRequest request
    ) {
        pointPolicyService.createPointPolicy(request.toDto());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{pointPolicyId}")
    @RequiredRole("ADMIN")
    public ResponseEntity<Void> updatePointPolicy(
            @PathVariable Long pointPolicyId,
            @RequestBody @Valid PointPolicyUpdateRequest request
    ) {
        pointPolicyService.updatePointPolicy(request.toDto(pointPolicyId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pointPolicyId}")
    @RequiredRole("ADMIN")
    public ResponseEntity<Void> deletePointPolicy(
            @PathVariable Long pointPolicyId
    ) {
        pointPolicyService.deletePointPolicy(pointPolicyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @RequiredRole("ADMIN")
    public ResponseEntity<List<PointPolicyResponse>> listPointPolicies() {
        List<PointPolicyResponse> response = pointPolicyService.listPointPolicies().stream()
                .map(PointPolicyResponse::from)
                .toList();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{pointPolicyId}")
    @RequiredRole("ADMIN")
    public ResponseEntity<PointPolicyResponse> getPointPolicy(
            @PathVariable Long pointPolicyId
    ) {
        PointPolicy pointPolicy = pointPolicyService.getPointPolicy(pointPolicyId);
        PointPolicyResponse response = PointPolicyResponse.from(pointPolicy);
        return ResponseEntity.ok().body(response);
    }
}

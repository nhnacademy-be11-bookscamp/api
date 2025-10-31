package store.bookscamp.api.pointpolicy.controller;

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
import store.bookscamp.api.pointpolicy.controller.request.PointPolicyCreateRequest;
import store.bookscamp.api.pointpolicy.controller.response.PointPolicyResponse;
import store.bookscamp.api.pointpolicy.controller.response.PointPolicyUpdateRequest;
import store.bookscamp.api.pointpolicy.entity.PointPolicy;
import store.bookscamp.api.pointpolicy.service.PointPolicyService;

@RestController
@RequestMapping("/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    @PostMapping
    public ResponseEntity<Void> createPointPolicy(
            @RequestBody @Valid PointPolicyCreateRequest request
    ) {
        pointPolicyService.createPointPolicy(request.toDto());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{pointPolicyId}")
    public ResponseEntity<Void> updatePointPolicy(
            @PathVariable Long pointPolicyId,
            @RequestBody @Valid PointPolicyUpdateRequest request
    ) {
        pointPolicyService.updatePointPolicy(request.toDto(pointPolicyId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pointPolicyId}")
    public ResponseEntity<Void> deletePointPolicy(
            @PathVariable Long pointPolicyId
    ) {
        pointPolicyService.deletePointPolicy(pointPolicyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PointPolicyResponse>> listPointPolicies() {
        List<PointPolicyResponse> response = pointPolicyService.listPointPolicies().stream()
                .map(PointPolicyResponse::from)
                .toList();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{pointPolicyId}")
    public ResponseEntity<PointPolicyResponse> getPointPolicy(
            @PathVariable Long pointPolicyId
    ) {
        PointPolicy pointPolicy = pointPolicyService.getPointPolicy(pointPolicyId);
        PointPolicyResponse response = PointPolicyResponse.from(pointPolicy);
        return ResponseEntity.ok().body(response);
    }
}

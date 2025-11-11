package store.bookscamp.api.pointhistory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.common.annotation.RequiredRole;
import store.bookscamp.api.pointhistory.controller.request.PointHistoryEarnRequest;
import store.bookscamp.api.pointhistory.controller.request.PointHistoryUseRequest;
import store.bookscamp.api.pointhistory.controller.response.PointHistoryResponse;
import store.bookscamp.api.pointhistory.service.PointHistoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "포인트내역 API", description = "PointHistory API입니다.")
public class PointHistoryController {

    private final PointHistoryService pointHistoryService;

    @PostMapping("/point-histories/earn")
    @Operation(summary = "earn Point", description = "포인트 적립 API")
    public ResponseEntity<Void> earnPoint(
            @RequestBody @Valid PointHistoryEarnRequest request) {

        pointHistoryService.earnPoint(request.toDto());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/point-histories/use")
    @Operation(summary = "use Point", description = "결제 시 포인트 사용 API")
    public ResponseEntity<Void> usePoint(
            @RequestBody @Valid PointHistoryUseRequest request) {

        pointHistoryService.usePoint(request.toDto());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/member/point-histories")
    @Operation(summary = "read Point", description = "유저 포인트 내역 조회 API")
    @RequiredRole("USER")
    public ResponseEntity<List<PointHistoryResponse>> getMyPointHistory(HttpServletRequest request) {

        Long memberId = Long.parseLong(request.getHeader("X-User-ID"));

        List<PointHistoryResponse> response = pointHistoryService.listMemberPoints(memberId)
                .stream()
                .map(PointHistoryResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}

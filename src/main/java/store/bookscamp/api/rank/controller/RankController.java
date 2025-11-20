package store.bookscamp.api.rank.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.rank.controller.request.RankGetRequest;
import store.bookscamp.api.rank.service.RankService;
import store.bookscamp.api.rank.service.dto.RankGetDto;

@RestController
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping("/rank")
    public ResponseEntity<RankGetRequest> getRank(
            HttpServletRequest request
    ){
        Long memberId = Long.valueOf(request.getHeader("X-User-ID"));

        RankGetDto memberRank = rankService.getMemberRank(memberId);
        RankGetRequest rankGetRequest = RankGetRequest.fromDto(memberRank);
        return ResponseEntity.ok(rankGetRequest);
    }
}

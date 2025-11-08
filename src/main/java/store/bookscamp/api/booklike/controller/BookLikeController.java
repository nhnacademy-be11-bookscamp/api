package store.bookscamp.api.booklike.controller;

import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.booklike.controller.request.BookLikeRequest;
import store.bookscamp.api.booklike.controller.response.BookLikeCountResponse;
import store.bookscamp.api.booklike.controller.response.BookLikeStatusResponse;
import store.bookscamp.api.booklike.service.BookLikeService;
import store.bookscamp.api.booklike.service.dto.BookLikeCountDto;
import store.bookscamp.api.booklike.service.dto.BookLikeStatusDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "좋아요 API", description = "BookLike API입니다")
public class BookLikeController {

    private final BookLikeService bookLikeService;

    @PutMapping("/books/like/{bookId}")
    public ResponseEntity<Void> toggleLike(
            HttpServletRequest servletRequest,
            @PathVariable Long bookId,
            @RequestBody BookLikeRequest request
    ){

        Long memberId = Long.valueOf(servletRequest.getHeader("X-User-ID"));

        bookLikeService.toggleLike(memberId, bookId, request.liked());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/books/{bookId}/like/count")
    public ResponseEntity<BookLikeCountResponse> getLikeCount(@PathVariable Long bookId){

        BookLikeCountDto likeCount = bookLikeService.getLikeCount(bookId);
        BookLikeCountResponse bookLikeCountResponse = BookLikeCountDto.toDto(likeCount);

        return ResponseEntity.ok(bookLikeCountResponse);
    }

    @GetMapping("/books/{bookId}/like/status")
    public ResponseEntity<BookLikeStatusResponse> getLikeStatus(
            HttpServletRequest servletRequest,
            @PathVariable Long bookId
    ){

        Long memberId = Long.valueOf(servletRequest.getHeader("X-User-ID"));

        BookLikeStatusDto likeStatus = bookLikeService.getLikeStatus(memberId, bookId);
        BookLikeStatusResponse bookLikeStatusResponse = BookLikeStatusDto.toDto(likeStatus);

        return ResponseEntity.ok(bookLikeStatusResponse);
    }
}

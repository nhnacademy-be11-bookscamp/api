package store.bookscamp.api.common;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import store.bookscamp.api.common.service.MinioService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/minio")
public class MinioTestController {

    private final MinioService minioService;

    @PostMapping("/upload/book")
    public ResponseEntity<String> uploadBookImage(@RequestParam MultipartFile file) {
        System.out.println("서버 잘 받았음");
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("파일크기: " + file.getSize());
        String url = minioService.uploadFile(file, "book");
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload/review")
    public ResponseEntity<String> uploadReviewImage(@RequestParam MultipartFile file) {
        String url = minioService.uploadFile(file, "review");
        return ResponseEntity.ok(url);
    }
}

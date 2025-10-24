package store.bookscamp.api.common.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket.book}")
    private String bookBucket;

    @Value("${minio.bucket.review}")
    private String reviewBucket;

    public String uploadFile(MultipartFile file, String type) {
        try {
            log.info("파일 업로드 요청 확인: {}", file.getOriginalFilename());

//            // 이름에 따라 버킷이름 저장
//            String bucketName = switch (type.toLowerCase()) {
//                case "book" -> bookBucket;
//                case "review" -> reviewBucket;
//                default -> throw new IllegalArgumentException("Invalid image type: " + type);
//            };
//
//            // 미니오에 버킷이름 존재 확인, 없으면 생성
//            boolean found = minioClient.bucketExists(
//                    BucketExistsArgs.builder().bucket(bucketName).build()
//            );
//            if (!found) {
//                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//            }
//
//            // 고유 파일명 생성
//            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//
//            // 미니오에 파일 업로드
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(fileName)
//                            .stream(file.getInputStream(), file.getSize(), -1) // 5MB 단위로 파일 업로드
//                            .contentType(file.getContentType()) // 브라우저에서 URL 접근 시 자동으로 파일 형식에 맞게 표시
//                            .build()
//            );
//
//            // URL 반환
//            return String.format("%s/%s/%s", minioUrl, bucketName, fileName);

            return "테스트 업로드 완료 (MinIO 연결 생략)";

        } catch (Exception e) {
            throw new RuntimeException("MinIO 파일 업로드 실패: " + e.getMessage(), e);
        }
    }
}

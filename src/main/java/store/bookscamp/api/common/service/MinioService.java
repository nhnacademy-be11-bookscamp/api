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
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * @param files 업로드할 파일들
     * @param type "book" or "review"
     * @return 프론트가 바로 쓸 수 있는 공개 URL 형태 (minioUrl/bucket/fileName)
     */
    public List<String> uploadFiles(List<MultipartFile> files, String type) {

        List<String> urls = new ArrayList<>();

        try {

            log.info("파일 업로드 요청 확인: {}", files.getFirst().getOriginalFilename());

            // 이름에 따라 버킷이름 저장
            String bucketName = switch (type.toLowerCase()) {
                case "book" -> bookBucket;
                case "review" -> reviewBucket;
                default -> throw new ApplicationException(ErrorCode.MINIO_BUCKET_NOT_FOUND);
            };

            // 미니오에 버킷이름 존재 확인, 없으면 생성
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );

                log.info("MinIO: bucket {} 생성 완료", bucketName);
            }

            // 고유 파일명 생성
            for( MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

                // 미니오에 파일 업로드
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(file.getInputStream(), file.getSize(), -1) // 5MB 단위로 파일 업로드
                                .contentType(file.getContentType()) // 브라우저에서 URL 접근 시 자동으로 파일 형식에 맞게 표시
                                .build()
                );

                String url = String.format("%s/%s/%s", minioUrl, bucketName, fileName);
                urls.add(url);
            }
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.MINIO_UPLOAD_FAILED);
        }
        return urls;
    }
}

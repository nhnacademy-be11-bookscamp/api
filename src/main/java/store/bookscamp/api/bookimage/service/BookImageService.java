package store.bookscamp.api.bookimage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.bookimage.service.dto.BookImageCreateDto;
import store.bookscamp.api.common.service.MinioService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookImageService {

    private final MinioService minioService;
    private final BookImageRepository bookImageRepository;

    @Transactional
    public List<String> uploadBookImages(BookImageCreateDto dto) {

        List<String> urls = minioService.uploadFiles(dto.files(), "book");

        // Book 없이 우선 URL만 저장
        // → 나중에 Book 등록 후 연관관계(book_id) 세팅 가능
        List<BookImage> savedImages = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            boolean isThumbnail = (i == 0);
            BookImage image = new BookImage(null, url, isThumbnail); // todo: Book은 임시로 null
            savedImages.add(bookImageRepository.save(image));
        }

        // URL 리스트 반환
        return urls;
    }
}

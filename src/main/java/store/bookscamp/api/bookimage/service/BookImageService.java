package store.bookscamp.api.bookimage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.bookimage.entity.BookImage;
import store.bookscamp.api.bookimage.repository.BookImageRepository;
import store.bookscamp.api.bookimage.service.dto.BookImageCreateDto;
import store.bookscamp.api.bookimage.service.dto.BookImageDeleteDto;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookImageService {

    private final BookImageRepository bookImageRepository;

    @Transactional
    public void createBookImage(BookImageCreateDto dto) {

        if (dto.book() == null) {
            throw new ApplicationException(ErrorCode.BOOK_NOT_FOUND);
        }

        List<BookImage> savedImages = new ArrayList<>();

        for (int i = 0; i < dto.imgUrls().size(); i++) {
            String url = dto.imgUrls().get(i);
            boolean isThumbnail = (i == 0);
            BookImage image = new BookImage(dto.book(), url, isThumbnail);
            savedImages.add(bookImageRepository.save(image));
        }
    }

    @Transactional
    public void deleteBookImage(BookImageDeleteDto dto){

        BookImage image = bookImageRepository.findById(dto.imageId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.IMAGE_NOT_FOUND));

        bookImageRepository.delete(image);

    }
}

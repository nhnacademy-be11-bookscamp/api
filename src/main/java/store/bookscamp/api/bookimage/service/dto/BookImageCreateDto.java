package store.bookscamp.api.bookimage.service.dto;

import org.springframework.web.multipart.MultipartFile;
import store.bookscamp.api.book.entity.Book;

import java.util.List;

public record BookImageCreateDto(
        Book book,
        List<MultipartFile> files
) {}

package store.bookscamp.api.bookimage.service.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record BookImageCreateDto(
    Long bookId,
    List<MultipartFile> files
) { }

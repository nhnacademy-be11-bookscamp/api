package store.bookscamp.api.book.controller.response;

import java.time.LocalDate;
import java.util.List;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.book.service.dto.BookDetailDto;
import store.bookscamp.api.category.service.dto.CategoryDto;
import store.bookscamp.api.tag.service.dto.TagDto;

public record BookInfoResponse(
        Long id,
        String title,
        String explanation,
        String content,
        String publisher,
        LocalDate publishDate,
        String contributors,
        String isbn,
        BookStatus status,
        boolean packable,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        long viewCount,
        List<CategoryDto> categoryList,
        List<TagDto> tagList,
        List<String> imageUrlList
) {
    public static BookInfoResponse from(BookDetailDto dto) {
        return new BookInfoResponse(
                dto.id(),
                dto.title(),
                dto.explanation(),
                dto.content(),
                dto.publisher(),
                dto.publishDate(),
                dto.contributors(),
                dto.isbn(),
                dto.status(),
                dto.packable(),
                dto.regularPrice(),
                dto.salePrice(),
                dto.stock(),
                dto.viewCount(),
                dto.categoryList(),
                dto.tagList(),
                dto.imageUrlList()
        );
    }
}

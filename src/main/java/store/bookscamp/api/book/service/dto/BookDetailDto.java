package store.bookscamp.api.book.service.dto;

import java.time.LocalDate;
import java.util.List;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;
import store.bookscamp.api.category.service.dto.CategoryDto;
import store.bookscamp.api.tag.service.dto.TagDto;

public record BookDetailDto(

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
    public static BookDetailDto from(
            Book book,
            List<CategoryDto> categoryList,
            List<TagDto> tagList,
            List<String> imageUrlList
    ) {
        return new BookDetailDto(
                book.getId(),
                book.getTitle(),
                book.getExplanation(),
                book.getContent(),
                book.getPublisher(),
                book.getPublishDate(),
                book.getContributors(),
                book.getIsbn(),
                book.getStatus(),
                book.isPackable(),
                book.getRegularPrice(),
                book.getSalePrice(),
                book.getStock(),
                book.getViewCount(),
                categoryList,
                tagList,
                imageUrlList
        );
    }
}

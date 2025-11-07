package store.bookscamp.api.book.service.dto;

import store.bookscamp.api.book.controller.request.AladinCreateRequest;
import store.bookscamp.api.book.controller.request.BookCreateRequest;

import java.time.LocalDate;
import java.util.List;

public record BookCreateDto(
        String title,
        String contributors,
        String publisher,
        String isbn,
        LocalDate publishDate,
        Integer regularPrice,
        Integer salePrice,
        Integer stock,
        boolean packable,
        String content,
        String explanation,
        List<String> imageUrls,
        List<Long> tagIds,
        Long categoryId
) {
    public static BookCreateDto from(BookCreateRequest req) {
        return new BookCreateDto(
                req.getTitle(),
                req.getContributors(),
                req.getPublisher(),
                req.getIsbn(),
                req.getPublishDate(),
                req.getRegularPrice(),
                req.getSalePrice(),
                req.getStock(),
                req.isPackable(),
                req.getContent(),
                req.getExplanation(),
                req.getImageUrls(),
                req.getTagIds(),
                req.getCategoryId()
        );
    }

    public static BookCreateDto from(AladinCreateRequest req) {
        return new BookCreateDto(
                req.getTitle(),
                req.getContributors(),
                req.getPublisher(),
                req.getIsbn(),
                req.getPublishDate(),
                req.getRegularPrice(),
                req.getSalePrice(),
                req.getStock(),
                req.isPackable(),
                req.getContent(),
                req.getExplanation(),
                req.getImageUrls(),
                req.getTagIds(),
                req.getCategoryId()
        );
    }
}

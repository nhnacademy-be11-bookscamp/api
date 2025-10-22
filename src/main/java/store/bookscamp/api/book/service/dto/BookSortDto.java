package store.bookscamp.api.book.service.dto;

import lombok.Getter;

@Getter
public class BookSortDto {

    private String title;
    private String publisher;
    private Long reviewCount;
    private double averageRating;

    public BookSortDto(String title, String publisher){
        this.title = title;
        this.publisher = publisher;
        this.reviewCount = 0L;
        this.averageRating = 0.0;
    }


    public BookSortDto(String title, String publisher, Long reviewCount){
        this.title = title;
        this.publisher = publisher;
        this.reviewCount = reviewCount;
        this.averageRating = 0.0;
    }

    public BookSortDto(String title, String publisher, double averageRating){
        this.title = title;
        this.publisher = publisher;
        this.reviewCount = 0L;
        this.averageRating = averageRating;
    }
}

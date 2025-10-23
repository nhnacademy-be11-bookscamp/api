package store.bookcamp.api.page.dto;

import lombok.Builder;
import lombok.Getter;
import store.bookscamp.api.book.entity.Book;
import store.bookscamp.api.book.entity.BookStatus;

import java.time.LocalDate;
import store.bookscamp.api.bookimage.entity.BookImage;

@Getter
@Builder
public class PageResponse {
    private Long id;
    private String title;
    private String explanation;
    private String publisher;
    private LocalDate publishDate;
    private Integer regularPrice;
    private Integer salePrice;
    private BookStatus status;
    private String contributor; // 기여자 이름
    private Boolean isThumbnail; // 썸네일 이미지 url

    public static PageResponse from(Book book) {
        String thumbnailUrl = String.valueOf(book.getImages().stream()
                .filter(BookImage::getIsThumbnail)
                .map(BookImage::getIsThumbnail)
                .findFirst()
                .orElse(null)); // 썸네일이 없으면 Null 처리

        return PageResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .explanation(book.getExplanation())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .regularPrice(book.getRegularPrice())
                .salePrice(book.getSalePrice())
                .status(book.getStatus())
                // Contributor가 null일 경우를 대비하여 Null-safe하게 처리
                .contributor(book.getContributor() != null ? book.getContributor().getContributors() : null)
                .isThumbnail(Boolean.valueOf(thumbnailUrl))
                // 썸네일 이미지 url
                .build();
    }
}

package store.bookscamp.api.book.entity;

import java.time.LocalDate;

public interface BookProjection {
    Long getId();
    String getTitle();
    String getExplanation();
    String getContent();
    String getPublisher();
    LocalDate getPublishDate();
    String getIsbn();
    String getContributors();
    Integer getRegularPrice();
    Integer getSalePrice();
    Integer getStock();
    Long getViewCount();
    Boolean getPackable();
    String getStatus();
    Double getAverageRating();
    Long getReviewCount();
}

package store.bookscamp.api.common.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class RestPageImpl<T> {

    private final List<T> content;
    private final int number;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;
    private final boolean first;
    private final int numberOfElements;
    private final boolean empty;

    public RestPageImpl(Page<T> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.numberOfElements = page.getNumberOfElements();
        this.empty = page.isEmpty();
    }
}
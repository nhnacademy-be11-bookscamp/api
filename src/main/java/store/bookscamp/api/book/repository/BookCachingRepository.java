package store.bookscamp.api.book.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import store.bookscamp.api.book.entity.BookCaching;

public interface BookCachingRepository
        extends ElasticsearchRepository<BookCaching, String> {
}
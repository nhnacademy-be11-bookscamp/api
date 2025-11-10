package store.bookscamp.api.book.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import store.bookscamp.api.book.entity.BookDocument;

public interface BookSearchRepository  extends ElasticsearchRepository<BookDocument,Long> {
}

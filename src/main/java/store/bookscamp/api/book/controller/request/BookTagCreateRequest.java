package store.bookscamp.api.book.controller.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookTagCreateRequest {
    private Long id;
    private String name;
}

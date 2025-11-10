package store.bookscamp.api.book.controller.request;


import java.util.List;

public record RerankerRequest(
        String query,
        List<String> texts
) {}
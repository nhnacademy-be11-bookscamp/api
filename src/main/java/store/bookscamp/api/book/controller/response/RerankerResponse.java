package store.bookscamp.api.book.controller.response;

public record RerankerResponse(
        int index,
        double score
) {}
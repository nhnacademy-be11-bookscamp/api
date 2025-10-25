package store.bookscamp.api.tag.exception;

public class TagAlreadyExists extends RuntimeException {
    public TagAlreadyExists(String message) {
        super(message);
    }
}

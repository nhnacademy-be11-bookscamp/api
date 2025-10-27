package store.bookscamp.api.common.exception;

public class MinioBucketNotFoundException extends ApplicationException {
    public MinioBucketNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

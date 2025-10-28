package store.bookscamp.api.common.exception;

public class MinioUploadFailedException extends ApplicationException {
    public MinioUploadFailedException(ErrorCode errorCode) {
        super(errorCode);
    }
}

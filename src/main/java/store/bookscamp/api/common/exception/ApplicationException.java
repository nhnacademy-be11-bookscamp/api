package store.bookscamp.api.common.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }
}

package store.bookscamp.api.tag.exception;

import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.common.exception.ApplicationException;

public class TagNotFoundException extends ApplicationException {
    public TagNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

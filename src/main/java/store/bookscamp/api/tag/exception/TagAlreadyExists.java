package store.bookscamp.api.tag.exception;

import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

public class TagAlreadyExists extends ApplicationException {
    public TagAlreadyExists(ErrorCode errorCode) {
        super(errorCode);
    }
}

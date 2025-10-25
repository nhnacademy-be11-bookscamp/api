package store.bookscamp.api.tag.exception;

import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.common.exception.ApplicationException;

public class TagNotFoundException extends ApplicationException {
    public TagNotFoundException(Long id) {
        super(ErrorCode.TAG_NOT_FOUND);
    }

    public TagNotFoundException(String name) {
        super(ErrorCode.TAG_NOT_FOUND);
    }

}

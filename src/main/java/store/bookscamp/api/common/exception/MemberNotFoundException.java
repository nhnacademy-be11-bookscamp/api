package store.bookscamp.api.common.exception;

public class MemberNotFoundException extends ApplicationException {
    public MemberNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

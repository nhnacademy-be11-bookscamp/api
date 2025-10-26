package store.bookscamp.api.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND(NOT_FOUND, "회원을 찾을 수 없습니다."),
    BOOK_NOT_FOUND(NOT_FOUND, "도서를 찾을 수 없습니다."),
    CART_NOT_FOUND(NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUNd(NOT_FOUND, "장바구니에 담긴 상품을 찾을 수 없습니다."),

    TAG_NOT_FOUND(NOT_FOUND, "태그를 찾을 수 없습니다."),
    TAG_ALREADY_EXISTS(BAD_REQUEST, "이미 존재하는 태그입니다."),
    ;


    private HttpStatus httpStatus;
    private String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}

package store.bookscamp.api.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    IMAGE_NOT_FOUND(NOT_FOUND, "이미지를 찾을 수 없습니다."),
    MINIO_BUCKET_NOT_FOUND(NOT_FOUND, "버킷을 찾을 수 없습니다."),
    MINIO_UPLOAD_FAILED(INTERNAL_SERVER_ERROR,"파일 업로드에 실패했습니다."),

    MEMBER_NOT_FOUND(NOT_FOUND, "회원을 찾을 수 없습니다."),

    BOOK_NOT_FOUND(NOT_FOUND, "도서를 찾을 수 없습니다."),

    CART_NOT_FOUND(NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(NOT_FOUND, "장바구니에 담긴 상품을 찾을 수 없습니다."),

    TAG_NOT_FOUND(NOT_FOUND, "태그를 찾을 수 없습니다."),
    TAG_ALREADY_EXISTS(BAD_REQUEST, "이미 존재하는 태그입니다."),

    POINT_POLICY_NOT_FOUND(NOT_FOUND, "포인트 정책을 찾을 수 없습니다."),
    ;


    private HttpStatus httpStatus;
    private String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}

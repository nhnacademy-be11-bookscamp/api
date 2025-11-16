package store.bookscamp.api.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    PARSE_ERROR(INTERNAL_SERVER_ERROR, "파싱할 수 없습니다."),
    IMAGE_NOT_FOUND(NOT_FOUND, "이미지를 찾을 수 없습니다."),
    MINIO_BUCKET_NOT_FOUND(NOT_FOUND, "버킷을 찾을 수 없습니다."),
    MINIO_UPLOAD_FAILED(INTERNAL_SERVER_ERROR,"파일 업로드에 실패했습니다."),

    MEMBER_NOT_FOUND(NOT_FOUND, "회원을 찾을 수 없습니다."),
    EMAIL_DUPLICATE(CONFLICT, "이미 존재하는 이메일입니다."),
    PHONE_DUPLICATE(CONFLICT, "이미 존재하는 전화번호입니다."),

    ADDRESS_NOT_FOUND(NOT_FOUND, "주소를 찾을 수 없습니다."),
    ADDRESS_LIMIT_EXCEEDED(BAD_REQUEST, "주소는 최대 10개까지 등록할 수 있습니다."),

    BOOK_NOT_FOUND(NOT_FOUND, "도서를 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(BAD_REQUEST, "재고가 부족합니다."),
    BOOK_NOT_AVAILABLE(BAD_REQUEST, "현재 판매 불가능한 도서입니다."),

    CATEGORY_NOT_FOUND(NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    INVALID_PARENT_CATEGORY_ID(BAD_REQUEST,"유효하지 않은 부모 카테고리 ID입니다."),
    CATEGORY_IN_USE(CONFLICT, "다른 도서에서 사용 중인 카테고리이므로 삭제할 수 없습니다."),
    CATEGORY_NAME_DUPLICATE(CONFLICT, "이미 사용 중인 카테고리 이름입니다."),

    CART_NOT_FOUND(NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(NOT_FOUND, "장바구니에 담긴 상품을 찾을 수 없습니다."),

    CONTRIBUTOR_NOT_FOUND(NOT_FOUND, "기여자를 찾을 수 없습니다."),
    CONTRIBUTOR_ALREADY_EXISTS(BAD_REQUEST, "이미 존재하는 기여자입니다."),

    TAG_NOT_FOUND(NOT_FOUND, "태그를 찾을 수 없습니다."),
    TAG_ALREADY_EXISTS(BAD_REQUEST, "이미 존재하는 태그입니다."),

    POINT_POLICY_NOT_FOUND(NOT_FOUND, "포인트 정책을 찾을 수 없습니다."),

    INSUFFICIENT_POINT(BAD_REQUEST, "포인트가 부족합니다."),

    RANK_NOT_FOUND(NOT_FOUND, "등급을 찾을 수 없습니다."),

    COUPON_NOT_FOUND(NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_ISSUE_NOT_FOUND(NOT_FOUND, "발급된 쿠폰을 찾을 수 없습니다."),
    COUPON_ISSUE_ALREADY_EXIST(CONFLICT, "이미 발급된 쿠폰이 존재합니다."),
    COUPON_ALREADY_USED(BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED(BAD_REQUEST, "만료된 쿠폰입니다."),
    COUPON_MIN_ORDER_AMOUNT_NOT_MET(BAD_REQUEST, "쿠폰 사용을 위한 최소 주문 금액을 충족하지 못했습니다."),
    COUPON_NOT_APPLICABLE_TO_BOOK(BAD_REQUEST, "해당 도서에 적용할 수 없는 쿠폰입니다."),
    COUPON_NOT_APPLICABLE_TO_CATEGORY(BAD_REQUEST, "해당 카테고리에 적용할 수 없는 쿠폰입니다."),
    INVALID_COUPON_DISCOUNT_TYPE(BAD_REQUEST, "유효하지 않은 쿠폰 할인 타입입니다."),

    PACKAGING_NOT_FOUND(NOT_FOUND, "포장지를 찾을 수 없습니다."),
    PACKAGING_DUPLICATE_RESOURCE(CONFLICT, "이미 존재하는 포장지입니다."),

    REDIS_CONNECTION_FAILED(INTERNAL_SERVER_ERROR, "캐시 서버에 연결할 수 없습니다."),
    CACHE_DATA_CORRUPTED(INTERNAL_SERVER_ERROR, "캐시 데이터가 손상되었습니다."),

    UNAUTHORIZED_USER(UNAUTHORIZED,"권한이 없습니다."),
    FORBIDDEN_USER(FORBIDDEN,"잘못된 권한입니다."),

    DELIVERY_POLICY_NOT_CONFIGURED(NOT_FOUND, "배송비 정책이 설정되지 않았습니다."),
    DELIVERY_POLICY_ALREADY_EXISTS(CONFLICT, "배송비 정책이 이미 존재합니다."),

    DELIVERY_POLICY_NOT_FOUND(NOT_FOUND, "배송 정책이 설정되지 않았습니다."),

    NON_MEMBER_INFO_REQUIRED(BAD_REQUEST, "비회원 주문 정보는 필수입니다."),
    COUPON_NOT_ALLOWED_FOR_NON_MEMBER(BAD_REQUEST, "비회원은 쿠폰을 사용할 수 없습니다."),
    POINT_NOT_ALLOWED_FOR_NON_MEMBER(BAD_REQUEST, "비회원은 포인트를 사용할 수 없습니다."),

    CART_ITEM_NOT_COMPLETE(BAD_REQUEST, "일부 장바구니 아이템을 찾을 수 없습니다."),

    ORDER_NOT_FOUND(NOT_FOUND, "주문을 찾을 수 없습니다."),

    REVIEW_NOT_FOUND(NOT_FOUND, "리뷰를 찾을 수 없습니다.")
    ;


    private HttpStatus httpStatus;
    private String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
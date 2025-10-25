package store.bookscamp.api.tag.exception;


/**
 * TODO : 조인이 얼마나 들어가는 지를 모르겠슨
 * 삭제 금지 규칙 : 이 태그가 도서에 연결되어 있으면 삭제 거부
 */
public class TagInUse extends RuntimeException {
    public TagInUse(String message) {
        super(message);
    }
}

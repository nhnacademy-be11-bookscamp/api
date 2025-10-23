package store.bookcamp.api.page.domain;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class Pagenation {

    private static final int DEFAULT_PAGE_NAV_SIZE = 10;

    // Page 객체에서 직접 가져옴
    private final int currentPage;    // 현재 페이지
    private final int pageSize;        // 한 페이지에 보일 게시물 사이즈
    private final int totalPages;        // 총 페이지 갯수
    private final long totalElements;

    private final int pageNavSize = DEFAULT_PAGE_NAV_SIZE;    // 페이지 보여줄 단위(사이즈)
    private final int startPageNo;    // 페이지의 시작번호
    private final int endPageNo;        // 페이지의 마지막번호

    private final boolean hasPreviousPageNav;    // 이전페이지
    private final boolean hasNextPageNav;        // 다음페이지
    private final boolean isFirstPage;    // 맨 첫번째 페이지
    private final boolean isLastPage;        // 맨 마지막 페이지

    public Pagenation(Page<?> page) {
        this.currentPage = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.isFirstPage = page.isFirst();
        this.isLastPage = page.isLast();

        this.startPageNo = (currentPage - 1) / pageNavSize * pageNavSize + 1;

        // 현재 페이지가 속한 블럭의 마지막 페이지 번호 계산
        this.endPageNo = Math.min(startPageNo + pageNavSize - 1, totalPages);

        // 3. 이전/다음 블럭 이동 가능 여부 계산
        // startPageNo가 1이 아니면 이전 블럭이 있음
        this.hasPreviousPageNav = startPageNo > 1;

        // endPageNo가 총 페이지 수보다 작으면 다음 블럭이 있음
        this.hasNextPageNav = endPageNo < totalPages;
    }
}
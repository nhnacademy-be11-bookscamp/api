package store.bookscamp.api.book.service.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

// record는 기본적으로 불변이고, 생성자/Getter/toString 등을 자동으로 만들어줘.
// Jackson 라이브러리가 record를 JSON으로 잘 변환하고 복구해줌!
public record BookBestSellerDto<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements
) {
    // Page 객체를 받아서 이 Record로 변환하는 정적 팩토리 메서드
    public static <T> BookBestSellerDto<T> from(Page<T> page) {
        return new BookBestSellerDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    // 필요할 때 다시 Spring Data의 Page 객체로 복구하는 메서드
    public Page<T> toPage() {
        return new PageImpl<>(
                content,
                PageRequest.of(pageNumber, pageSize),
                totalElements
        );
    }
}
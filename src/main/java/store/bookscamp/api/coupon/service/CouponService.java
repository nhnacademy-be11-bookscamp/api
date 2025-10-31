package store.bookscamp.api.coupon.service;

import static store.bookscamp.api.common.exception.ErrorCode.BOOK_NOT_FOUND;
import static store.bookscamp.api.common.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static store.bookscamp.api.coupon.entity.TargetType.BOOK;
import static store.bookscamp.api.coupon.entity.TargetType.CATEGORY;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.book.repository.BookRepository;
import store.bookscamp.api.category.repository.CategoryRepository;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.coupon.entity.Coupon;
import store.bookscamp.api.coupon.repository.CouponRepository;
import store.bookscamp.api.coupon.service.dto.CouponCreateDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long createCoupon(CouponCreateDto dto) {
        validTarget(dto);
        Coupon coupon = dto.toEntity();
        return couponRepository.save(coupon)
                .getId();
    }

    private void validTarget(CouponCreateDto dto) {
        if (dto.targetType().equals(BOOK)) {
            bookRepository.findById(dto.targetId())
                    .orElseThrow(() -> new ApplicationException(BOOK_NOT_FOUND));
        }
        if (dto.targetType().equals(CATEGORY)) {
            categoryRepository.findById(dto.targetId())
                    .orElseThrow(() -> new ApplicationException(CATEGORY_NOT_FOUND));
        }
    }

    public List<Coupon> listCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
    }
}

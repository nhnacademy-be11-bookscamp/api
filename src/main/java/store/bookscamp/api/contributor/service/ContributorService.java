package store.bookscamp.api.contributor.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.contributor.controller.request.ContributorAddRequest;
import store.bookscamp.api.contributor.controller.request.ContributorUpdateRequest;
import store.bookscamp.api.contributor.controller.response.ContributorResponse;
import store.bookscamp.api.contributor.entity.Contributor;
import store.bookscamp.api.contributor.repository.ContributorRepository;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContributorService {

    private final ContributorRepository contributorRepository;


    @Transactional(readOnly = false)
    public ContributorResponse create(ContributorAddRequest request) {
        // 중복 기여자 존재 시 예외 발생
        if (contributorRepository.existsByContributors(request.getContributors())) {
            throw new ApplicationException(ErrorCode.CONTRIBUTOR_ALREADY_EXISTS);
        }

        Contributor saved = contributorRepository.save(request.toEntity());
        return ContributorResponse.from(saved);
    }

    // 기여자 단일 조회
    public ContributorResponse get(Long id) {
        Contributor contributor = contributorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CONTRIBUTOR_NOT_FOUND));
        return ContributorResponse.from(contributor);
    }

    // 기여자 전체 조회
    public List<ContributorResponse> list() {
        return contributorRepository.findAll().stream()
                .map(ContributorResponse::from)
                .toList();
    }

    // 기여자 수정
    @Transactional(readOnly = false)
    public ContributorResponse update(Long id, ContributorUpdateRequest request) {
        Contributor contributor = contributorRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CONTRIBUTOR_NOT_FOUND));
        contributor.changeContributors(request.getContributors());
        return ContributorResponse.from(contributor);
    }

    // 기여자 삭제
    @Transactional(readOnly = false)
    public void delete(Long id) {
        if (!contributorRepository.existsById(id)) {
            throw new ApplicationException(ErrorCode.CONTRIBUTOR_NOT_FOUND);
        }
        contributorRepository.deleteById(id);
    }

}

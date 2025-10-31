package store.bookscamp.api.contributor.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.contributor.controller.request.ContributorAddRequest;
import store.bookscamp.api.contributor.controller.request.ContributorUpdateRequest;
import store.bookscamp.api.contributor.controller.response.ContributorResponse;
import store.bookscamp.api.contributor.service.ContributorService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/contributors")
public class ContributorController {

    private final ContributorService contributorService;

    @PostMapping
    public ContributorResponse create(@Valid @RequestBody ContributorAddRequest request) {
        return contributorService.create(request);
    }

    @GetMapping( "/{id}")
    public ContributorResponse get(@PathVariable Long id) {
        return contributorService.get(id);
    }

    @GetMapping
    public List<ContributorResponse> list() {
        return contributorService.list();
    }

    @PutMapping("/{id}")
    public ContributorResponse update(@PathVariable Long id, @Valid @RequestBody ContributorUpdateRequest request) {
        return contributorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contributorService.delete(id);
    }

}


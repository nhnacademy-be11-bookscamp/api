package store.bookscamp.api.tag.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.bookscamp.api.tag.controller.request.TagCreateRequest;
import store.bookscamp.api.tag.controller.request.TagUpdateRequest;
import store.bookscamp.api.tag.controller.response.TagGetResponse;
import store.bookscamp.api.tag.service.TagService;
import store.bookscamp.api.tag.service.dto.TagCreateDto;
import store.bookscamp.api.tag.service.dto.TagGetDto;

@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagGetResponse> create(@Valid @RequestBody TagCreateRequest request) {
        TagGetDto created = tagService.create(TagCreateRequest.toDto(request));
        TagGetResponse body = TagGetResponse.fromDto(created);
        return ResponseEntity.created(URI.create("/tags/" + body.getId())).body(body); // "/tags" -> "/admin/tags"
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagGetResponse> get(@PathVariable Long id) {
        TagGetDto dto = tagService.getById(id);
        return ResponseEntity.ok(TagGetResponse.fromDto(dto));
    }

    @GetMapping
    public ResponseEntity<List<TagGetResponse>> getAll() {
        List<TagGetResponse> list = tagService.getAll().stream()
                .map(TagGetResponse::fromDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagGetResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody TagUpdateRequest request) {
        TagGetDto updated = tagService.update(id, new TagCreateDto(request.getName()));
        return ResponseEntity.ok(TagGetResponse.fromDto(updated));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}





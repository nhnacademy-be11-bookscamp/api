package store.bookscamp.api.tag.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.tag.controller.TagRequest;
import store.bookscamp.api.tag.controller.TagResponse;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.exception.BadRequestException;
import store.bookscamp.api.tag.exception.TagNotFoundException;
import store.bookscamp.api.tag.repository.TagRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = request.getName();

        if (tagRepository.existsByName(name)) {
            throw new BadRequestException("이미 존재하는 태그 이름입니다: " + name);
        }
        // TODO : create 에서 문제 생기는 거 확인하기
        Tag tag = Tag.create(name);
        tag = tagRepository.save(tag);
        return TagResponse.from(tag);
    }

    public TagResponse getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        return TagResponse.from(tag);
    }

    public List<TagResponse> getAll() {
        return tagRepository.findAll().stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        String newName = request.getName();
        if( !tag.getName().equals(newName) && tagRepository.existsByName(newName)) {
            throw new BadRequestException(newName);
        }

        tag.changeName(newName);
        return TagResponse.from(tag);
    }

    @Transactional
    public void deleteById(Long id) {
        if( !tagRepository.existsById(id)) {
            throw new TagNotFoundException(id);
        }
        tagRepository.deleteById(id);
    }
}

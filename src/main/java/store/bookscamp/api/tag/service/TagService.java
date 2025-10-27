package store.bookscamp.api.tag.service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.bookscamp.api.common.exception.ErrorCode;
import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.exception.TagAlreadyExists;
import store.bookscamp.api.tag.exception.TagNotFoundException;
import store.bookscamp.api.tag.repository.TagRepository;
import store.bookscamp.api.tag.service.dto.TagCreateDto;
import store.bookscamp.api.tag.service.dto.TagGetDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public TagGetDto create(TagCreateDto createDto) {
        String name = createDto.getName();

        if(tagRepository.existsByName(name)) {
            throw new TagAlreadyExists(ErrorCode.TAG_ALREADY_EXISTS);
        }

        Tag tag = tagRepository.save(Tag.create(name));
        return TagGetDto.from(tag);
    }

    public TagGetDto getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(
                        ErrorCode.TAG_NOT_FOUND));
        return TagGetDto.from(tag);
    }

    public List<TagGetDto> getAll() {
        return tagRepository.findAll().stream()
                .map(TagGetDto::from)
                .toList();
    }

    @Transactional
    public TagGetDto update(Long id, TagCreateDto updateDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(ErrorCode.TAG_NOT_FOUND));

        String newName = updateDto.getName();
        if (!tag.getName().equals(newName) && tagRepository.existsByName(newName)) {
            throw new TagAlreadyExists(ErrorCode.TAG_ALREADY_EXISTS);
        }

        tag.changeName(newName);
        return TagGetDto.from(tag);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new TagNotFoundException(ErrorCode.TAG_NOT_FOUND);
        }
        tagRepository.deleteById(id);
    }
}

// TagControllerTest

package store.bookscamp.api.tag.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import store.bookscamp.api.tag.entity.Tag;
import store.bookscamp.api.tag.repository.TagRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TagControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @Autowired
    TagRepository tagRepository;

    private static final MediaType JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    @Test
    @DisplayName("POST /admin/tags - 생성 성공(201 + Location + body)")
    void create_ok() throws Exception {
        String body = """
        {"name":"java"}
        """;

        mockMvc.perform(post("/admin/tags")
                        .contentType(JSON_UTF8)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/tags/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("java"));
    }

    @Test
    @DisplayName("POST /admin/tags - 중복이면 400")
    void create_dup() throws Exception {
        tagRepository.save(Tag.create("java"));

        String body = """
        {"name":"java"}
        """;

        mockMvc.perform(post("/admin/tags")
                        .contentType(JSON_UTF8)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/tags/{id} - 조회 성공")
    void get_ok() throws Exception {
        Tag saved = tagRepository.save(Tag.create("spring"));

        mockMvc.perform(get("/admin/tags/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    @DisplayName("GET /admin/tags/{id} - 없으면 404")
    void get_404() throws Exception {
        mockMvc.perform(get("/admin/tags/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /admin/tags - 전체 목록")
    void list_ok() throws Exception {
        LongStream.rangeClosed(1, 3)
                .forEach(i -> tagRepository.save(Tag.create("t" + i)));

        mockMvc.perform(get("/admin/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("t1", "t2", "t3")));
    }

    @Test
    @DisplayName("PUT /admin/tags/{id} - 수정 성공")
    void update_ok() throws Exception {
        Tag saved = tagRepository.save(Tag.create("old"));

        String body = """
        {"name":"new"}
        """;

        mockMvc.perform(put("/admin/tags/{id}", saved.getId())
                        .contentType(JSON_UTF8)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("new"));
    }

    @Test
    @DisplayName("PUT /admin/tags/{id} - 이름 중복이면 400")
    void update_dup() throws Exception {
        Tag t1 = tagRepository.save(Tag.create("a"));
        tagRepository.save(Tag.create("dup"));

        String body = """
        {"name":"dup"}
        """;

        mockMvc.perform(put("/admin/tags/{id}", t1.getId())
                        .contentType(JSON_UTF8)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/tags/{id} - 삭제 204")
    void delete_ok() throws Exception {
        Tag saved = tagRepository.save(Tag.create("del"));

        mockMvc.perform(delete("/admin/tags/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /admin/tags/{id} - 없으면 404")
    void delete_404() throws Exception {
        mockMvc.perform(delete("/admin/tags/{id}", 12345L))
                .andExpect(status().isNotFound());
    }
}

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

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String ADMIN = "ADMIN";

    @Test
    @DisplayName("POST /admin/tags - 태그 생성 성공(201 + Location + body)")
    void create_ok() throws Exception {
        // given
        String body = """
        {"name":"java"}
        """;

        mockMvc.perform(post("/admin/tags")
                        .contentType(JSON_UTF8)
                        .content(body)
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/tags/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("java"));
    }

    @Test
    @DisplayName("POST /admin/tags - 중복이면 400 Bad Request")
    void create_dup() throws Exception {
        tagRepository.save(Tag.create("java"));

        String body = """
        {"name":"java"}
        """;

        mockMvc.perform(post("/admin/tags")
                        .contentType(JSON_UTF8)
                        .content(body)
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/tags/{id} - 태그 ID로 조회 성공")
    void get_ok() throws Exception {
        // given
        Tag saved = tagRepository.save(Tag.create("spring"));

        // when & then
        mockMvc.perform(get("/admin/tags/{id}", saved.getId())
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    @DisplayName("GET /admin/tags/{id} - 존재하지 않는 ID면 404 Not Found")
    void get_404() throws Exception {
        mockMvc.perform(get("/admin/tags/{id}", 999L)
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /admin/tags - Page 형태로 전체 목록 조회 확인")
    void getAll_ok() throws Exception {
        LongStream.rangeClosed(1, 3)
                .forEach(i -> tagRepository.save(Tag.create("t" + i)));

        mockMvc.perform(get("/admin/tags")
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].name",
                        containsInAnyOrder("t1", "t2", "t3")))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0)); // 현재 페이지 index
    }

    @Test
    @DisplayName("GET /admin/tags - 기본 페이징(size=5, id DESC) 동작 확인")
    void getAll_paging_default() throws Exception {
        LongStream.rangeClosed(1, 8)
                .forEach(i -> tagRepository.save(Tag.create("t" + i)));

        mockMvc.perform(get("/admin/tags")
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].name").value("t8"))
                .andExpect(jsonPath("$.content[1].name").value("t7"))
                .andExpect(jsonPath("$.content[2].name").value("t6"))
                .andExpect(jsonPath("$.content[3].name").value("t5"))
                .andExpect(jsonPath("$.content[4].name").value("t4"))
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("PUT /admin/tags/{id} - 태그 이름 수정 성공")
    void update_ok() throws Exception {
        // given
        Tag saved = tagRepository.save(Tag.create("old"));

        String body = """
        {"name":"new"}
        """;

        // when & then
        mockMvc.perform(put("/admin/tags/{id}", saved.getId())
                        .contentType(JSON_UTF8)
                        .content(body)
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("new"));
    }

    @Test
    @DisplayName("PUT /admin/tags/{id} - 이름 중복이면 400 Bad Request")
    void update_dup() throws Exception {
        Tag t1 = tagRepository.save(Tag.create("a"));
        tagRepository.save(Tag.create("dup"));

        String body = """
        {"name":"dup"}
        """;

        // when & then
        mockMvc.perform(put("/admin/tags/{id}", t1.getId())
                        .contentType(JSON_UTF8)
                        .content(body)
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/tags/{id} - 삭제 성공 204 No Content")
    void delete_ok() throws Exception {
        // given
        Tag saved = tagRepository.save(Tag.create("del"));

        mockMvc.perform(delete("/admin/tags/{id}", saved.getId())
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /admin/tags/{id} - 존재하지 않는 ID 삭제 시도 시 404 Not Found")
    void delete_404() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/tags/{id}", 12345L)
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isNotFound());
    }
}
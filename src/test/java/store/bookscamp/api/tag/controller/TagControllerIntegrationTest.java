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

    // ADMIN 권한을 부여하기 위한 상수 정의 (PackagingControllerTest 참고)
    private static final String ROLE_HEADER = "X-User-Role";
    private static final String ADMIN = "ADMIN";

    @Test
    @DisplayName("POST /admin/tags - 태그 생성 성공(201 + Location + body)")
    void create_ok() throws Exception {
        // given
        String body = """
        {"name":"java"}
        """;

        // when & then
        mockMvc.perform(post("/admin/tags")
                        .contentType(JSON_UTF8)
                        .content(body)
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isCreated())
                // Location 헤더가 /tags/{id} 패턴을 따르는지 확인
                .andExpect(header().string("Location", matchesPattern("/tags/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("java"));
    }

    @Test
    @DisplayName("POST /admin/tags - 중복이면 400 Bad Request")
    void create_dup() throws Exception {
        // given: 이미 존재하는 태그 생성
        tagRepository.save(Tag.create("java"));

        String body = """
        {"name":"java"}
        """;

        // when & then
        mockMvc.perform(post("/admin/tags")
                        .contentType(JSON_UTF8)
                        .content(body)
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
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
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    @DisplayName("GET /admin/tags/{id} - 존재하지 않는 ID면 404 Not Found")
    void get_404() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/tags/{id}", 999L)
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /admin/tags - Page 형태로 전체 목록 조회 확인")
    void getAll_ok() throws Exception {
        // given: 3개의 태그 저장
        LongStream.rangeClosed(1, 3)
                .forEach(i -> tagRepository.save(Tag.create("t" + i)));

        // when & then
        mockMvc.perform(get("/admin/tags")
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                // Page<T> 구조 확인
                .andExpect(jsonPath("$.content", hasSize(3)))
                // 저장된 모든 이름이 포함되었는지 확인
                .andExpect(jsonPath("$.content[*].name",
                        containsInAnyOrder("t1", "t2", "t3")))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0)); // 현재 페이지 index
    }

    @Test
    @DisplayName("GET /admin/tags - 기본 페이징(size=5, id DESC) 동작 확인")
    void getAll_paging_default() throws Exception {
        // given: 8개의 태그 저장 (id: 1~8). 기본 size=5, sort=id,DESC
        LongStream.rangeClosed(1, 8)
                .forEach(i -> tagRepository.save(Tag.create("t" + i)));

        // when & then
        mockMvc.perform(get("/admin/tags")
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                // 첫 페이지 content size = 5
                .andExpect(jsonPath("$.content", hasSize(5)))
                // id DESC 기준이므로 t8, t7, t6, t5, t4 순으로 정렬되었는지 확인
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
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("new"));
    }

    @Test
    @DisplayName("PUT /admin/tags/{id} - 이름 중복이면 400 Bad Request")
    void update_dup() throws Exception {
        // given: 수정 대상 태그 (t1)와 중복될 이름 (dup) 태그 준비
        Tag t1 = tagRepository.save(Tag.create("a"));
        tagRepository.save(Tag.create("dup"));

        String body = """
        {"name":"dup"}
        """;

        // when & then
        mockMvc.perform(put("/admin/tags/{id}", t1.getId())
                        .contentType(JSON_UTF8)
                        .content(body)
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/tags/{id} - 삭제 성공 204 No Content")
    void delete_ok() throws Exception {
        // given
        Tag saved = tagRepository.save(Tag.create("del"));

        // when & then
        mockMvc.perform(delete("/admin/tags/{id}", saved.getId())
                        // FIX: X-User-Role 헤더를 통해 ADMIN 권한 부여
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
package store.bookscamp.api.packaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import store.bookscamp.api.packaging.entity.Packaging;
import store.bookscamp.api.packaging.repository.PackagingRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class PackagingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PackagingRepository packagingRepository;

    private static final MediaType JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String ADMIN = "ADMIN";

    private org.springframework.test.web.servlet.request.RequestPostProcessor adminHeaders() {
        return request -> {
            request.addHeader(ROLE_HEADER, ADMIN);
            return request;
        };
    }

    @Test
    @DisplayName("POST /admin/packagings/create — 생성 성공 후 DB 반영")
    void create_success() throws Exception {
        mockMvc.perform(post("/admin/packagings/create")
                        .contentType(JSON_UTF8)
                        .header(ROLE_HEADER, ADMIN)
                        .content("""
                            {"name":"선물포장 A","price":1500,"imageUrl":["https://img/1.png"]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("포장지 등록이 완료되었습니다."));

        List<Packaging> all = packagingRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("선물포장 A");
        assertThat(all.get(0).getPrice()).isEqualTo(1500);
        assertThat(all.get(0).getImageUrl()).isEqualTo("https://img/1.png");
    }

    @Test
    @DisplayName("POST /admin/packagings/create — 중복 이름이면 409")
    void create_duplicateName_conflict() throws Exception {
        // given
        packagingRepository.save(new Packaging("중복", 1000, "u1"));

        // when & then
        mockMvc.perform(post("/admin/packagings/create")
                        .header(ROLE_HEADER, ADMIN)
                        .contentType(JSON_UTF8)
                        .content("""
                            {"name":"중복","price":2000,"imageUrls":["u2"]}
                        """))
                .andExpect(status().isConflict()); // ApplicationException -> 409 가정
    }

    @Test
    @DisplayName("GET /admin/packagings/{id} — 단건 조회 성공")
    void get_one_success() throws Exception {
        Packaging saved = packagingRepository.save(new Packaging("P1", 1000, "u1"));

        mockMvc.perform(get("/admin/packagings/{id}", saved.getId())
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("P1"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.imageUrl").value("u1"));
    }

    @Test
    @DisplayName("GET /admin/packagings — 전체 조회 성공")
    void get_all_success() throws Exception {
        packagingRepository.save(new Packaging("A", 1000, "u1"));
        packagingRepository.save(new Packaging("B", 2000, "u2"));

        mockMvc.perform(get("/admin/packagings")
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    @DisplayName("PUT /admin/packagings/{id}/update — 수정 성공")
    void update_success() throws Exception {
        Packaging saved = packagingRepository.save(new Packaging("Old", 1200, "old.png"));

        mockMvc.perform(put("/admin/packagings/{id}/update", saved.getId())
                        .contentType(JSON_UTF8)
                        .header(ROLE_HEADER, ADMIN)
                        .content("""
                            {"name":"New","price":2200,"imageUrl":["new.png"]}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("포장지 정보가 수정되었습니다."));

        Packaging reloaded = packagingRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("New");
        assertThat(reloaded.getPrice()).isEqualTo(2200);
        assertThat(reloaded.getImageUrl()).isEqualTo("new.png");
    }

    @Test
    @DisplayName("PUT /admin/packagings/{id}/update — 다른 엔티티와 이름 중복이면 409")
    void update_duplicateName_conflict() throws Exception {
        Packaging a = packagingRepository.save(new Packaging("A", 1000, "a.png"));
        packagingRepository.save(new Packaging("B", 2000, "b.png"));

        mockMvc.perform(put("/admin/packagings/{id}/update", a.getId())
                        .contentType(JSON_UTF8)
                        .header(ROLE_HEADER, ADMIN)
                        .content("""
                            {"name":"B","price":1300,"imagePatch":false}
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /admin/packagings/{id}/delete — 삭제 성공")
    void delete_success() throws Exception {
        Packaging saved = packagingRepository.save(new Packaging("TBD", 900, "x.png"));

        mockMvc.perform(delete("/admin/packagings/{id}/delete", saved.getId())
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("포장지가 삭제되었습니다."));

        assertThat(packagingRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    @DisplayName("DELETE /admin/packagings/{id}/delete — 없는 자원 삭제 시 404")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/admin/packagings/{id}/delete", 999_999L)
                        .header(ROLE_HEADER, ADMIN))
                .andExpect(status().isNotFound());
    }
}
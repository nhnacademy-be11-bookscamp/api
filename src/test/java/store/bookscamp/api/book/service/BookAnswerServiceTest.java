package store.bookscamp.api.book.service;


import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import store.bookscamp.api.book.entity.BookDocument;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookAnswerServiceTest {

    private void setField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("buildPrompt() - 정상적으로 프롬프트 생성됨")
    void buildPrompt_success() throws Exception {

        BookAnswerService service = new BookAnswerService();

        BookDocument b = BookDocument.builder()
                .id(10L)
                .title("테스트 책")
                .explanation("요약문")
                .build();

        Method m = BookAnswerService.class.getDeclaredMethod(
                "buildPrompt", String.class, List.class
        );
        m.setAccessible(true);

        String prompt = (String) m.invoke(service, "자바", List.of(b));

        assertThat(prompt).contains("사용자가 다음과 같은 검색을 했습니다: '자바'");
        assertThat(prompt).contains("테스트 책");
        assertThat(prompt).contains("요약문");
        assertThat(prompt).contains("출력 예");
    }

    @Test
    @DisplayName("generateAnswer() - 내부 예외 발생 시 fallback 메시지 반환")
    void generateAnswer_exception() {

        BookAnswerService service = new BookAnswerService();
        setField(service, "geminiApiKey", "FAKE_KEY");

        Map<String, Object> result = service.generateAnswer("검색", null);

        assertThat(result.get("result"))
                .isEqualTo("AI 응답 생성 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("generateAnswer() - candidates가 없을 때 fallback 메시지")
    void generateAnswer_noCandidates_case() {

        BookAnswerService service = new BookAnswerService();
        setField(service, "geminiApiKey", "FAKE_KEY");

        Map<String, Object> result = service.generateAnswer("검색", List.of());

        assertThat(result.get("result")).isIn(
                "AI 응답 생성 중 오류가 발생했습니다.",
                "추천 결과를 생성하지 못했습니다."
        );
    }
}


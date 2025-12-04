package store.bookscamp.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ApiApplicationTests {

	@Test
	void contextLoads() {
        // 애플리케이션 컨텍스트가 성공적으로 로드되는지 확인하기 위한 테스트입니다.
        // 빈 메서드지만 실행 시 컨텍스트 초기화 과정을 검증합니다.
	}

}

package store.bookscamp.api.common.annotation;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RoleCheckAspectTest {

    @InjectMocks
    private RoleCheckAspect roleCheckAspect;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RequiredRole requiredRole;

    @BeforeEach
    void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("성공: 헤더의 Role과 요구 Role이 일치하면 통과")
    void checkRole_Success() {
        String role = "USER";
        given(request.getHeader("X-User-Role")).willReturn(role);
        given(requiredRole.value()).willReturn(role);

        assertThatCode(() -> roleCheckAspect.checkRole(requiredRole))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: 헤더에 Role 정보가 없음 (UNAUTHORIZED_USER)")
    void checkRole_Fail_NoHeader() {
        given(request.getHeader("X-User-Role")).willReturn(null);

        assertThatThrownBy(() -> roleCheckAspect.checkRole(requiredRole))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_USER);
    }

    @Test
    @DisplayName("실패: 헤더의 Role과 요구 Role이 다름 (FORBIDDEN_USER)")
    void checkRole_Fail_Forbidden() {
        given(request.getHeader("X-User-Role")).willReturn("USER");
        given(requiredRole.value()).willReturn("ADMIN"); // 요구 권한은 ADMIN

        assertThatThrownBy(() -> roleCheckAspect.checkRole(requiredRole))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_USER);
    }
}
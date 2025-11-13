package store.bookscamp.api.common.annotation;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import store.bookscamp.api.common.exception.ApplicationException;
import store.bookscamp.api.common.exception.ErrorCode;

@Aspect
@Component
public class RoleCheckAspect {
    @Before("@annotation(requiredRole)")
    public void checkRole(RequiredRole requiredRole){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String userRole = request.getHeader("X-User-Role");
        String required = requiredRole.value();

        if(userRole == null){
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_USER);
        }

        if ("ADMIN".equals(required) && "USER".equals(userRole)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN_USER);
        }

        if ("USER".equals(required) && ("USER".equals(userRole) || "ADMIN".equals(userRole))) {
        } else if (!userRole.equals(required)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN_USER);
        }

    }

}

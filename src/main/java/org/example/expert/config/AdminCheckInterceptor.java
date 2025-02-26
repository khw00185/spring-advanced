package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class AdminCheckInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AdminCheckInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        if(!requestUri.startsWith("/admin")) {
            return true;
        }

        UserRole userRole = UserRole.valueOf((String) request.getAttribute("userRole"));

        if(!UserRole.ADMIN.equals(userRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
            return false;
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        logger.info("요청시각: {}, 요청 URL: {}", timestamp, requestUri);
        return true;
    }
}

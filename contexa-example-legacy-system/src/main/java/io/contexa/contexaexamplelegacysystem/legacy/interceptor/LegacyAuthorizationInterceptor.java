package io.contexa.contexaexamplelegacysystem.legacy.interceptor;

import io.contexa.contexaexamplelegacysystem.legacy.annotation.LegacyRole;
import io.contexa.contexaexamplelegacysystem.legacy.filter.LegacyAuthFilter;
import io.contexa.contexaexamplelegacysystem.legacy.model.LegacyUserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

/**
 * Legacy authorization interceptor.
 * Checks @LegacyRole annotations and IP-based access restrictions.
 * This is completely independent of Spring Security.
 */
@Slf4j
public class LegacyAuthorizationInterceptor implements HandlerInterceptor {

    private static final Set<String> INTERNAL_NETWORKS = Set.of(
            "127.0.0.1", "0:0:0:0:0:0:0:1", "192.168.1.", "10.0.0.");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        LegacyRole roleAnnotation = handlerMethod.getMethodAnnotation(LegacyRole.class);
        if (roleAnnotation == null) {
            roleAnnotation = handlerMethod.getBeanType().getAnnotation(LegacyRole.class);
        }

        if (roleAnnotation == null) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Authentication required\"}");
            return false;
        }

        LegacyUserSession userSession =
                (LegacyUserSession) session.getAttribute(LegacyAuthFilter.SESSION_USER_KEY);
        if (userSession == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Authentication required\"}");
            return false;
        }

        // Check role
        String[] requiredRoles = roleAnnotation.value();
        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> userSession.getRoles().contains(role));

        if (!hasRole) {
            log.error("[Legacy Auth] Access denied: user={}, required={}, has={}",
                    userSession.getUsername(), Arrays.toString(requiredRoles), userSession.getRoles());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Insufficient permissions\"}");
            return false;
        }

        return true;
    }

    private boolean isInternalNetwork(String ip) {
        if (ip == null) return false;
        return INTERNAL_NETWORKS.stream().anyMatch(ip::startsWith);
    }
}

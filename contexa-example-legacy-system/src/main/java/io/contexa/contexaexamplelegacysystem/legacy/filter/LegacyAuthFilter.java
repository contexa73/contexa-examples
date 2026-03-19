package io.contexa.contexaexamplelegacysystem.legacy.filter;

import io.contexa.contexaexamplelegacysystem.legacy.model.LegacyUser;
import io.contexa.contexaexamplelegacysystem.legacy.model.LegacyUserSession;
import io.contexa.contexaexamplelegacysystem.legacy.service.LegacyUserService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Legacy authentication filter handling 3 authentication methods:
 * 1. Form Login: POST /legacy/login (username + password)
 * 2. API Token: X-Auth-Token header
 * 3. Remember-Me: legacy-remember-me cookie
 *
 * This is a typical enterprise legacy auth filter - NO Spring Security involved.
 */
@Slf4j
public class LegacyAuthFilter implements Filter {

    public static final String SESSION_USER_KEY = "legacyUser";
    public static final String REMEMBER_ME_COOKIE = "legacy-remember-me";

    private final LegacyUserService userService;

    public LegacyAuthFilter(LegacyUserService userService) {
        this.userService = userService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        // Handle login POST (before public path check)
        if ("POST".equals(request.getMethod()) && "/legacy/login".equals(path)) {
            handleFormLogin(request, response);
            return;
        }

        // Handle logout
        if ("/legacy/logout".equals(path)) {
            handleLogout(request, response);
            return;
        }

        // Skip static resources and public pages
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check existing session
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_USER_KEY) != null) {
            chain.doFilter(request, response);
            return;
        }

        // Try API token authentication
        String apiToken = request.getHeader("X-Auth-Token");
        if (apiToken != null && !apiToken.isBlank()) {
            LegacyUser user = userService.authenticateByToken(apiToken);
            if (user != null) {
                createSession(request, user, "TOKEN");
                chain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid API token\"}");
            return;
        }

        // Try remember-me cookie
        Cookie rememberMeCookie = findCookie(request, REMEMBER_ME_COOKIE);
        if (rememberMeCookie != null) {
            LegacyUser user = userService.authenticateByRememberMe(rememberMeCookie.getValue());
            if (user != null) {
                createSession(request, user, "REMEMBER_ME");
                chain.doFilter(request, response);
                return;
            }
        }

        // Protected path without authentication
        if (isProtectedPath(path)) {
            response.sendRedirect("/legacy/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private void handleFormLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        LegacyUser user = userService.authenticate(username, password);
        if (user == null) {
            response.sendRedirect("/legacy/login?error=invalid");
            return;
        }

        createSession(request, user, "FORM");

        // Set remember-me cookie if requested
        if ("on".equals(rememberMe)) {
            String cookieValue = UUID.randomUUID().toString();
            userService.registerRememberMe(cookieValue, user);
            Cookie cookie = new Cookie(REMEMBER_ME_COOKIE, cookieValue);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        log.info("[Legacy Auth] Login success: user={}, method=FORM, dept={}",
                user.username(), user.department());
        response.sendRedirect("/legacy/dashboard");
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            LegacyUserSession userSession = (LegacyUserSession) session.getAttribute(SESSION_USER_KEY);
            if (userSession != null) {
                log.info("[Legacy Auth] Logout: user={}", userSession.getUsername());
            }
            session.removeAttribute(SESSION_USER_KEY);
        }

        // Clear remember-me cookie
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect("/legacy/login?logout=true");
    }

    private void createSession(HttpServletRequest request, LegacyUser user, String authMethod) {
        LegacyUserSession userSession = new LegacyUserSession(
                user.userId(), user.username(), user.displayName(),
                user.roles(), request.getRemoteAddr(), authMethod,
                user.department(), user.accessLevel());

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, userSession);

        log.info("[Legacy Auth] Session created: user={}, method={}, roles={}",
                user.username(), authMethod, user.roles());
    }

    private boolean isPublicPath(String path) {
        return path.equals("/") ||
                path.equals("/legacy/login") ||
                path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/favicon") ||
                path.startsWith("/error") ||
                path.startsWith("/default-ui") ||
                path.startsWith("/.well-known");
    }

    private boolean isProtectedPath(String path) {
        return path.startsWith("/legacy/") && !path.equals("/legacy/login");
    }

    private Cookie findCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }
}

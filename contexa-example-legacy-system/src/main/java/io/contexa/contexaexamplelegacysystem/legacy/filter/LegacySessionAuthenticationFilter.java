package io.contexa.contexaexamplelegacysystem.legacy.filter;

import io.contexa.contexaexamplelegacysystem.legacy.model.LegacyUserSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Puts the user the legacy login left in the session into the SecurityContext on every request.
 * The legacy login itself (LegacyAuthFilter) is unchanged.
 */
public class LegacySessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null
                && session.getAttribute(LegacyAuthFilter.SESSION_USER_KEY) instanceof LegacyUserSession user) {
            List<SimpleGrantedAuthority> roles = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(user.getUserId(), null, roles));
        }
        chain.doFilter(request, response);
    }
}

package io.contexa.example.identityasep.filter;

import io.contexa.example.identityasep.exception.SecurityPolicyViolationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter that intentionally throws exceptions within the security filter chain.
 *
 * Purpose: demonstrate that ASEPFilter catches these exceptions and delegates
 * them to @SecurityControllerAdvice handlers — something Spring's standard
 * @ControllerAdvice cannot do because it only operates at the servlet level.
 *
 * Trigger paths:
 *   /api/trigger-auth-error    -> AuthenticationException
 *   /api/trigger-access-error  -> AccessDeniedException
 *   /api/trigger-custom-error  -> SecurityPolicyViolationException
 */
public class CustomSecurityExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("/api/trigger-auth-error".equals(path)) {
            throw new InsufficientAuthenticationException(
                    "Simulated authentication failure in security filter chain");
        }

        if ("/api/trigger-access-error".equals(path)) {
            throw new AccessDeniedException(
                    "Simulated access denied in security filter chain");
        }

        if ("/api/trigger-custom-error".equals(path)) {
            throw new SecurityPolicyViolationException(
                    "POLICY_IP_BLOCKED",
                    "Request blocked by security policy",
                    "Source IP 203.0.113.50 is in the blocked range");
        }

        filterChain.doFilter(request, response);
    }
}

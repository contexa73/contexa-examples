package io.contexa.example.identityasep.advice;

import io.contexa.contexaidentity.security.core.asep.annotation.CaughtException;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityControllerAdvice;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityExceptionHandler;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityPrincipal;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityRequestHeader;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityResponseBody;
import io.contexa.example.identityasep.exception.SecurityPolicyViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom @SecurityControllerAdvice that handles exceptions thrown
 * within the Spring Security filter chain.
 *
 * Unlike @ControllerAdvice (which only catches controller-level exceptions),
 * this class catches exceptions from the ENTIRE security filter chain
 * because ASEPFilter wraps filterChain.doFilter() in a try-catch
 * and delegates to handlers registered via @SecurityControllerAdvice.
 *
 * ExceptionTranslationFilter only catches exceptions from filters AFTER it.
 * ASEPFilter catches exceptions from ALL filters before the servlet.
 */
@SecurityControllerAdvice
public class CustomSecurityExceptionAdvice {

    /**
     * Handles AuthenticationException thrown in the filter chain.
     */
    @SecurityExceptionHandler(AuthenticationException.class)
    @SecurityResponseBody
    public ResponseEntity<Map<String, Object>> handleAuthException(
            @CaughtException AuthenticationException ex,
            @SecurityRequestHeader(value = "User-Agent", required = false, defaultValue = "unknown") String userAgent,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "AUTHENTICATION_FAILED");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        body.put("handler", "CustomSecurityExceptionAdvice");
        body.put("handlerMethod", "handleAuthException");
        body.put("userAgent", userAgent);
        body.put("flow", "CustomSecurityExceptionFilter -> ASEPFilter -> @SecurityControllerAdvice");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles AccessDeniedException thrown in the filter chain.
     */
    @SecurityExceptionHandler(AccessDeniedException.class)
    @SecurityResponseBody
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            @CaughtException AccessDeniedException ex,
            @SecurityPrincipal Object principal,
            HttpServletRequest request) {

        String user = principal != null ? principal.toString() : "anonymous";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "ACCESS_DENIED");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        body.put("user", user);
        body.put("handler", "CustomSecurityExceptionAdvice");
        body.put("handlerMethod", "handleAccessDenied");
        body.put("flow", "CustomSecurityExceptionFilter -> ASEPFilter -> @SecurityControllerAdvice");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handles custom SecurityPolicyViolationException.
     *
     * This demonstrates that ASEP can handle ANY exception type,
     * not just standard Spring Security exceptions.
     */
    @SecurityExceptionHandler(SecurityPolicyViolationException.class)
    @SecurityResponseBody
    public ResponseEntity<Map<String, Object>> handlePolicyViolation(
            @CaughtException SecurityPolicyViolationException ex,
            @SecurityPrincipal Object principal,
            @SecurityRequestHeader(value = "X-Forwarded-For", required = false, defaultValue = "unknown") String clientIp,
            HttpServletRequest request) {

        String user = principal != null ? principal.toString() : "anonymous";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "POLICY_VIOLATION");
        body.put("policyCode", ex.getPolicyCode());
        body.put("message", ex.getMessage());
        body.put("detail", ex.getDetail());
        body.put("path", request.getRequestURI());
        body.put("user", user);
        body.put("clientIp", clientIp);
        body.put("handler", "CustomSecurityExceptionAdvice");
        body.put("handlerMethod", "handlePolicyViolation");
        body.put("flow", "CustomSecurityExceptionFilter -> ASEPFilter -> @SecurityControllerAdvice");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}

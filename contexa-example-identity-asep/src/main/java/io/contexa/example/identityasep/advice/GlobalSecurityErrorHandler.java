package io.contexa.example.identityasep.advice;

import io.contexa.contexaidentity.security.core.asep.annotation.SecurityControllerAdvice;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityExceptionHandler;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityPrincipal;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityRequestHeader;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityResponseBody;
import io.contexa.example.identityasep.dto.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Global security exception handler using ASEP annotations.
 *
 * @SecurityControllerAdvice - Scans for security exception handlers
 * @SecurityExceptionHandler - Handles specific security exceptions
 * @SecurityResponseBody - Serializes return value to response body
 */
@SecurityControllerAdvice
public class GlobalSecurityErrorHandler {

    @SecurityExceptionHandler(AuthenticationException.class)
    @SecurityResponseBody
    public ErrorResponse handleAuthError(
            AuthenticationException ex,
            @SecurityRequestHeader(value = "X-Request-Id", required = false, defaultValue = "none") String requestId) {
        return new ErrorResponse("AUTH_FAILED", ex.getMessage(), requestId);
    }

    @SecurityExceptionHandler(AccessDeniedException.class)
    @SecurityResponseBody
    public ErrorResponse handleAccessDenied(
            AccessDeniedException ex,
            @SecurityPrincipal Object principal) {
        String user = principal != null ? principal.toString() : "anonymous";
        return new ErrorResponse("ACCESS_DENIED", "Forbidden for user: " + user, null);
    }
}

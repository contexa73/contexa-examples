package io.contexa.example.identityasep.controller;

import io.contexa.contexaidentity.security.core.asep.annotation.SecurityPrincipal;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityRequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates @SecurityRequestHeader annotation.
 *
 * Injects HTTP headers within security filter chain context.
 */
@RestController
@RequestMapping("/api/secure")
public class SecureApiController {

    @GetMapping("/info")
    public Map<String, Object> info(
            @SecurityPrincipal Object principal,
            @SecurityRequestHeader(value = "User-Agent", required = false) String userAgent,
            @SecurityRequestHeader(value = "X-Request-Id", required = false, defaultValue = "none") String requestId) {

        return Map.of(
                "user", principal != null ? principal.toString() : "anonymous",
                "userAgent", userAgent != null ? userAgent : "unknown",
                "requestId", requestId
        );
    }
}

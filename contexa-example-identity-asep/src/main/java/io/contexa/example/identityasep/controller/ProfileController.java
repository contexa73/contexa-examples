package io.contexa.example.identityasep.controller;

import io.contexa.contexaidentity.security.core.asep.annotation.AuthenticationObject;
import io.contexa.contexaidentity.security.core.asep.annotation.SecurityPrincipal;
import io.contexa.contexaidentity.security.core.asep.annotation.SecuritySessionAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates ASEP argument injection annotations.
 *
 * @SecurityPrincipal - Injects current security principal
 * @AuthenticationObject - Injects full Authentication object
 * @SecuritySessionAttribute - Injects session attribute
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/profile")
    public Map<String, Object> profile(
            @SecurityPrincipal Object principal,
            @AuthenticationObject Authentication auth) {

        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("principal", principal != null ? principal.toString() : "anonymous");
        response.put("username", auth.getName());
        response.put("authorities", authorities);
        response.put("authenticated", auth.isAuthenticated());
        response.put("principalClass", principal != null ? principal.getClass().getSimpleName() : "null");
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}

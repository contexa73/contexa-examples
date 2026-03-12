package io.contexa.example.identitydsl.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST API endpoints protected by CustomDynamicAuthorizationManager.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/profile")
    public Map<String, Object> profile(@AuthenticationPrincipal UserDetails user) {
        return Map.of(
                "username", user != null ? user.getUsername() : "anonymous",
                "authorities", user != null ? user.getAuthorities().toString() : "none"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}

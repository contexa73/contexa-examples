package io.contexa.contexaexampleidentitymfamulti.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> authorities = auth != null ? auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList() : List.of("ROLE_ANONYMOUS");
        String username = auth != null ? auth.getName() : "anonymousUser";
        return Map.of(
                "username", username,
                "authorities", authorities,
                "authMethod", "Multi MFA"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}

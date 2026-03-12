package io.contexa.example.iamdynamicauth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource controller for URL dynamic authorization demo.
 *
 * Each endpoint is protected by URL-level policies stored in the database.
 * The policies use AI security expressions (#trust, #ai) evaluated at runtime
 * by CustomDynamicAuthorizationManager.
 *
 * URL policy mapping (see data.sql):
 * - /api/resources/public/**    -> isAuthenticated()
 * - /api/resources/normal/**    -> #trust.hasActionIn('ALLOW','MONITOR') and hasRole('USER')
 * - /api/resources/sensitive/** -> #trust.requiresAnalysisWithAction('ALLOW','MONITOR') and hasRole('USER')
 * - /api/resources/admin/**     -> hasRole('ADMIN') and #trust.requiresAnalysisWithAction('ALLOW')
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Public resource - requires authentication only (no AI analysis).
     * Policy: isAuthenticated()
     */
    @GetMapping("/public/{id}")
    public Map<String, Object> publicResource(@PathVariable String id) {
        return buildResponse(id, "PUBLIC", "No AI analysis required - authentication only");
    }

    /**
     * Normal resource - AI analysis preferred.
     * Policy: #trust.hasActionIn('ALLOW','MONITOR') and hasRole('USER')
     */
    @GetMapping("/normal/{id}")
    public Map<String, Object> normalResource(@PathVariable String id) {
        return buildResponse(id, "NORMAL", "AI analysis preferred - ALLOW or MONITOR action accepted");
    }

    /**
     * Sensitive resource - AI analysis required.
     * Policy: #trust.requiresAnalysisWithAction('ALLOW','MONITOR') and hasRole('USER')
     */
    @GetMapping("/sensitive/{id}")
    public Map<String, Object> sensitiveResource(@PathVariable String id) {
        return buildResponse(id, "SENSITIVE", "AI analysis required - must complete before access");
    }

    /**
     * Admin resource - ADMIN role + strict AI analysis.
     * Policy: hasRole('ADMIN') and #trust.requiresAnalysisWithAction('ALLOW')
     */
    @GetMapping("/admin/{id}")
    public Map<String, Object> adminResource(@PathVariable String id) {
        return buildResponse(id, "ADMIN", "ADMIN role + AI ALLOW action required");
    }

    private Map<String, Object> buildResponse(String resourceId, String securityLevel, String policyDescription) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("timestamp", LocalDateTime.now().format(FORMATTER));
        response.put("resourceId", resourceId);
        response.put("securityLevel", securityLevel);
        response.put("policyDescription", policyDescription);
        response.put("user", auth.getName());
        response.put("authorities", authorities);
        return response;
    }
}

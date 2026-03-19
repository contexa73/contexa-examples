package io.contexa.contexaexamplelegacysystem.protected_api.controller;

import io.contexa.contexacommon.annotation.Protectable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API endpoints protected by Contexa AI Zero Trust.
 * <p>
 * These endpoints are part of the LEGACY system's business logic,
 * but annotated with @Protectable so Contexa monitors them.
 * <p>
 * Legacy auth (LegacyAuthFilter) handles authentication.
 * Contexa AuthBridge reads legacy session and enables AI analysis.
 * If AI detects anomaly -> CHALLENGE/BLOCK via Contexa MFA/Zero Trust.
 */
@RestController
@RequestMapping("/api/customers")
public class ProtectedCustomerController {

    /**
     * Customer list - AI-monitored access.
     * Normal users: ALLOW. Suspicious behavior: CHALLENGE.
     */
    @Protectable
    @GetMapping
    public List<Map<String, Object>> getCustomers() {
        return List.of(
                Map.of("id", "C001", "name", "Acme Corp", "revenue", "12,500,000 KRW",
                        "contact", "kim@acme.com", "tier", "Enterprise"),
                Map.of("id", "C002", "name", "GlobalTech", "revenue", "8,200,000 KRW",
                        "contact", "park@globaltech.com", "tier", "Premium"),
                Map.of("id", "C003", "name", "DataFlow Inc", "revenue", "5,100,000 KRW",
                        "contact", "lee@dataflow.com", "tier", "Standard"),
                Map.of("id", "C004", "name", "CloudNine Systems", "revenue", "15,800,000 KRW",
                        "contact", "choi@cloudnine.com", "tier", "Enterprise"),
                Map.of("id", "C005", "name", "SecureNet Korea", "revenue", "3,200,000 KRW",
                        "contact", "jung@securenet.kr", "tier", "Standard")
        );
    }

    /**
     * Customer export - synchronous AI analysis (immediate decision).
     * High-sensitivity operation: AI analyzes BEFORE data is returned.
     */
    @Protectable(sync = true)
    @GetMapping("/export")
    public Map<String, Object> exportCustomers() {
        return Map.of(
                "totalRecords", 5,
                "exportTime", System.currentTimeMillis(),
                "data", "CSV export data would be here...",
                "sensitiveFields", List.of("revenue", "contact")
        );
    }
}

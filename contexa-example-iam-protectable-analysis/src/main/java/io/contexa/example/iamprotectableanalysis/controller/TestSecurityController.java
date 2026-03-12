package io.contexa.example.iamprotectableanalysis.controller;

import io.contexa.contexacommon.enums.ZeroTrustAction;
import io.contexa.example.iamprotectableanalysis.service.TestSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Security flow test REST controller.
 * Calls TestSecurityService @Protectable methods and returns results as JSON.
 */
@Slf4j
@RestController
@RequestMapping("/api/security-test")
@RequiredArgsConstructor
public class TestSecurityController {

    private final TestSecurityService testSecurityService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @GetMapping("/public/{resourceId}")
    public ResponseEntity<Map<String, Object>> testPublicData(@PathVariable String resourceId) {
        return executeTest(() -> testSecurityService.getPublicData(resourceId), resourceId, "NOT_REQUIRED");
    }

    @GetMapping("/normal/{resourceId}")
    public ResponseEntity<Map<String, Object>> testNormalData(@PathVariable String resourceId) {
        return executeTest(() -> testSecurityService.getNormalData(resourceId), resourceId, "PREFERRED");
    }

    @GetMapping("/sensitive/{resourceId}")
    public ResponseEntity<Map<String, Object>> testSensitiveData(@PathVariable String resourceId) {
        return executeTest(() -> testSecurityService.getSensitiveData(resourceId), resourceId, "REQUIRED");
    }

    @GetMapping("/critical/{resourceId}")
    public ResponseEntity<Map<String, Object>> testCriticalData(@PathVariable String resourceId) {
        return executeTest(() -> testSecurityService.getCriticalData(resourceId), resourceId, "STRICT");
    }

    @GetMapping("/bulk")
    public ResponseEntity<Map<String, Object>> testBulkData() {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try {
            String result = testSecurityService.getBulkData();
            long processingTime = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("timestamp", timestamp);
            response.put("user", auth != null ? auth.getName() : "anonymous");
            response.put("analysisRequirement", "PREFERRED (Runtime Interception)");
            response.put("dataLength", result.length());
            response.put("recordCount", 10000);
            response.put("processingTime", processingTime);
            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e, "bulk", "PREFERRED (Runtime Interception)", processingTime, timestamp, auth));
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e, "bulk", "PREFERRED (Runtime Interception)", processingTime, timestamp, auth));
        }
    }

    private ResponseEntity<Map<String, Object>> executeTest(
            java.util.function.Supplier<String> serviceCall, String resourceId, String analysisRequirement) {

        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try {
            String result = serviceCall.get();
            long processingTime = System.currentTimeMillis() - startTime;
            return ResponseEntity.ok(createSuccessResponse(result, resourceId, analysisRequirement, processingTime, timestamp, auth));

        } catch (AccessDeniedException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e, resourceId, analysisRequirement, processingTime, timestamp, auth));
        } catch (IllegalArgumentException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("timestamp", timestamp);
            response.put("resourceId", resourceId);
            response.put("error", "ValidationError");
            response.put("message", e.getMessage());
            response.put("processingTime", processingTime);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e, resourceId, analysisRequirement, processingTime, timestamp, auth));
        }
    }

    private Map<String, Object> createSuccessResponse(
            String data, String resourceId, String analysisRequirement,
            long processingTime, String timestamp, Authentication auth) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("timestamp", timestamp);
        response.put("user", auth != null ? auth.getName() : "anonymous");
        response.put("resourceId", resourceId);
        response.put("analysisRequirement", analysisRequirement);
        response.put("data", data);
        response.put("processingTime", processingTime);
        return response;
    }

    private Map<String, Object> createErrorResponse(
            Exception e, String resourceId, String analysisRequirement,
            long processingTime, String timestamp, Authentication auth) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("timestamp", timestamp);
        response.put("user", auth != null ? auth.getName() : "anonymous");
        response.put("resourceId", resourceId);
        response.put("analysisRequirement", analysisRequirement);
        response.put("error", e.getClass().getSimpleName());
        response.put("message", e.getMessage());
        response.put("processingTime", processingTime);

        if (e instanceof AccessDeniedException) {
            response.put("blocked", true);
            response.put("blockReason", extractBlockReason(e.getMessage()));
        }
        return response;
    }

    private String extractBlockReason(String message) {
        if (message == null) return "Unknown";
        if (message.contains(ZeroTrustAction.BLOCK.name())) return "LLM Action: BLOCK";
        if (message.contains(ZeroTrustAction.PENDING_ANALYSIS.name())) return "Analysis not completed (timeout)";
        if (message.contains("MONITOR") && message.contains("STRICT")) return "STRICT mode requires ALLOW action";
        if (message.contains("Access Denied") || message.contains("Access is denied")) return "Policy evaluation failed";
        return message;
    }
}

package io.contexa.example.iamprotectableanalysis.controller;

import io.contexa.contexacommon.enums.ZeroTrustAction;
import io.contexa.example.iamprotectableanalysis.event.LlmAnalysisEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.Map;

/**
 * LLM analysis SSE controller.
 * Streams real-time LLM analysis process to clients via Server-Sent Events.
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class LlmAnalysisSseController {

    private final LlmAnalysisEventPublisher eventPublisher;

    @GetMapping(value = "/llm-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToLlmAnalysis() {
        return eventPublisher.addEmitter();
    }

    @GetMapping(value = "/llm-analysis/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToUserLlmAnalysis(Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        return eventPublisher.addEmitter(userId);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "active",
                "subscriberCount", eventPublisher.getSubscriberCount(),
                "timestamp", System.currentTimeMillis()
        ));
    }

    @PostMapping("/test-event")
    public ResponseEntity<Map<String, Object>> publishTestEvent(
            @RequestParam String eventType,
            @RequestParam(defaultValue = "testUser") String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Double riskScore,
            @RequestParam(required = false) Double confidence) {

        switch (eventType) {
            case "CONTEXT_COLLECTED":
                eventPublisher.publishContextCollected(userId, "/api/test/resource", "REQUIRED");
                break;
            case "LAYER1_START":
                eventPublisher.publishLayer1Start(userId, "/api/test/resource");
                break;
            case "LAYER1_COMPLETE":
                eventPublisher.publishLayer1Complete(userId,
                        action != null ? action : ZeroTrustAction.ALLOW.name(),
                        riskScore != null ? riskScore : 0.2,
                        confidence != null ? confidence : 0.85,
                        "Test reasoning for Layer1 analysis", "none", 500L);
                break;
            case "LAYER2_START":
                eventPublisher.publishLayer2Start(userId, "/api/test/resource", "Escalation reason");
                break;
            case "LAYER2_COMPLETE":
                eventPublisher.publishLayer2Complete(userId,
                        action != null ? action : ZeroTrustAction.CHALLENGE.name(),
                        riskScore != null ? riskScore : 0.6,
                        confidence != null ? confidence : 0.75,
                        "Test reasoning for Layer2 analysis", "T1078", 1500L);
                break;
            case "DECISION_APPLIED":
                eventPublisher.publishDecisionApplied(userId,
                        action != null ? action : ZeroTrustAction.ALLOW.name(),
                        "LAYER1", "/api/test/resource");
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Unknown event type: " + eventType,
                        "validTypes", new String[]{
                                "CONTEXT_COLLECTED", "LAYER1_START", "LAYER1_COMPLETE",
                                "LAYER2_START", "LAYER2_COMPLETE", "DECISION_APPLIED"}));
        }

        return ResponseEntity.ok(Map.of(
                "success", true, "eventType", eventType,
                "userId", userId, "timestamp", System.currentTimeMillis()));
    }

    @PostMapping("/simulate-analysis")
    public ResponseEntity<Map<String, Object>> simulateAnalysis(
            @RequestParam(defaultValue = "demoUser") String userId,
            @RequestParam(defaultValue = "false") boolean escalate,
            @RequestParam(defaultValue = "ALLOW") String finalAction) {

        String requestPath = "/api/security-test/sensitive/demo-resource";

        new Thread(() -> {
            try {
                eventPublisher.publishContextCollected(userId, requestPath, "REQUIRED");
                Thread.sleep(500);
                eventPublisher.publishLayer1Start(userId, requestPath);
                Thread.sleep(1000);

                if (escalate) {
                    eventPublisher.publishLayer1Complete(userId, ZeroTrustAction.ESCALATE.name(),
                            0.5, 0.35, "Insufficient confidence, escalating to Layer2", "none", 1000L);
                    Thread.sleep(500);
                    eventPublisher.publishLayer2Start(userId, requestPath, "Low confidence in Layer1");
                    Thread.sleep(2000);
                    eventPublisher.publishLayer2Complete(userId, finalAction, 0.4, 0.85,
                            "Deep analysis completed by Claude", "T1078", 2000L);
                    Thread.sleep(300);
                    eventPublisher.publishDecisionApplied(userId, finalAction, "LAYER2", requestPath);
                } else {
                    double riskScore = ZeroTrustAction.BLOCK.name().equals(finalAction) ? 0.9 :
                            ZeroTrustAction.CHALLENGE.name().equals(finalAction) ? 0.6 : 0.2;
                    eventPublisher.publishLayer1Complete(userId, finalAction, riskScore, 0.85,
                            "Analysis completed by Llama 8B", "none", 1000L);
                    Thread.sleep(300);
                    eventPublisher.publishDecisionApplied(userId, finalAction, "LAYER1", requestPath);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return ResponseEntity.ok(Map.of(
                "success", true, "message", "Analysis simulation started",
                "userId", userId, "escalate", escalate, "finalAction", finalAction));
    }
}

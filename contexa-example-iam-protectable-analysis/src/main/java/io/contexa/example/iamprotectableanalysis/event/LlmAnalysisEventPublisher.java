package io.contexa.example.iamprotectableanalysis.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * LLM analysis event publisher.
 * Manages SSE emitters and broadcasts analysis events to subscribed clients.
 */
@Component
@Slf4j
public class LlmAnalysisEventPublisher {

    private final List<SseEmitter> globalEmitters = new CopyOnWriteArrayList<>();
    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 300_000L;

    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitter.onCompletion(() -> globalEmitters.remove(emitter));
        emitter.onTimeout(() -> globalEmitters.remove(emitter));
        emitter.onError(e -> globalEmitters.remove(emitter));
        globalEmitters.add(emitter);

        try {
            emitter.send(SseEmitter.event().name("connected")
                    .data("{\"status\":\"connected\",\"timestamp\":" + System.currentTimeMillis() + "}"));
        } catch (IOException e) {
            log.error("[LlmAnalysisEventPublisher] Connection event send failed", e);
        }
        return emitter;
    }

    public SseEmitter addEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        List<SseEmitter> userList = userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());

        emitter.onCompletion(() -> { userList.remove(emitter); globalEmitters.remove(emitter); });
        emitter.onTimeout(() -> { userList.remove(emitter); globalEmitters.remove(emitter); });
        emitter.onError(e -> { userList.remove(emitter); globalEmitters.remove(emitter); });

        userList.add(emitter);
        globalEmitters.add(emitter);

        try {
            emitter.send(SseEmitter.event().name("connected")
                    .data("{\"status\":\"connected\",\"userId\":\"" + userId + "\",\"timestamp\":" + System.currentTimeMillis() + "}"));
        } catch (IOException e) {
            log.error("[LlmAnalysisEventPublisher] Connection event send failed for userId: {}", userId, e);
        }
        return emitter;
    }

    public void publishEvent(LlmAnalysisEvent event) {
        if (globalEmitters.isEmpty()) return;

        String eventData = event.toJson();
        String eventType = event.getType();
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : globalEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(eventData));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        globalEmitters.removeAll(deadEmitters);
    }

    public void publishContextCollected(String userId, String requestPath, String analysisRequirement) {
        publishEvent(LlmAnalysisEvent.contextCollected(userId, requestPath, analysisRequirement));
    }

    public void publishLayer1Start(String userId, String requestPath) {
        publishEvent(LlmAnalysisEvent.layer1Start(userId, requestPath));
    }

    public void publishLayer1Complete(String userId, String action, Double riskScore,
            Double confidence, String reasoning, String mitre, Long elapsedMs) {
        publishEvent(LlmAnalysisEvent.layer1Complete(userId, action, riskScore, confidence, reasoning, mitre, elapsedMs));
    }

    public void publishLayer2Start(String userId, String requestPath, String reason) {
        publishEvent(LlmAnalysisEvent.layer2Start(userId, requestPath, reason));
    }

    public void publishLayer2Complete(String userId, String action, Double riskScore,
            Double confidence, String reasoning, String mitre, Long elapsedMs) {
        publishEvent(LlmAnalysisEvent.layer2Complete(userId, action, riskScore, confidence, reasoning, mitre, elapsedMs));
    }

    public void publishDecisionApplied(String userId, String action, String layer, String requestPath) {
        publishEvent(LlmAnalysisEvent.decisionApplied(userId, action, layer, requestPath));
    }

    public void publishError(String userId, String message) {
        publishEvent(LlmAnalysisEvent.error(userId, message));
    }

    public int getSubscriberCount() {
        return globalEmitters.size();
    }
}

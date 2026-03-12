package io.contexa.example.aillm.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE publisher for advisor lifecycle events.
 * Simplified version of ZeroTrustSsePublisher from contexa-iam.
 */
@Slf4j
@Component
public class AdvisorLogPublisher {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter(300_000L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void publish(String advisorName, String phase, String data) {
        AdvisorLogEvent event = new AdvisorLogEvent(advisorName, phase, data, System.currentTimeMillis());
        try {
            String json = objectMapper.writeValueAsString(event);
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("advisor-log")
                            .data(json));
                } catch (Exception e) {
                    emitters.remove(emitter);
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish advisor log event", e);
        }
    }
}

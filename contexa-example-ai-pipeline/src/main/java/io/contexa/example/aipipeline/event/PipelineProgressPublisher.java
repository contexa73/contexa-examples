package io.contexa.example.aipipeline.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE publisher for pipeline step progress events.
 * Simplified version of ZeroTrustSsePublisher from contexa-iam.
 */
@Slf4j
@Component
public class PipelineProgressPublisher {

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

    public void publishStepProgress(String stepName, String status, long elapsedMs, String message) {
        PipelineProgressEvent event = new PipelineProgressEvent(stepName, status, elapsedMs, message);
        try {
            String json = objectMapper.writeValueAsString(event);
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("pipeline-progress")
                            .data(json));
                } catch (Exception e) {
                    emitters.remove(emitter);
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish pipeline progress event", e);
        }
    }
}

package io.contexa.example.aipipeline.controller;

import io.contexa.example.aipipeline.event.PipelineProgressPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE endpoint for pipeline step progress events.
 * Client connects via EventSource to receive real-time step updates.
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class PipelineSseController {

    private final PipelineProgressPublisher progressPublisher;

    @GetMapping(value = "/pipeline", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter pipelineProgress() {
        return progressPublisher.addEmitter();
    }
}

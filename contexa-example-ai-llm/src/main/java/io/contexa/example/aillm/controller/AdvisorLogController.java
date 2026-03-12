package io.contexa.example.aillm.controller;

import io.contexa.example.aillm.event.AdvisorLogPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE endpoint for advisor lifecycle events.
 * Client connects via EventSource to receive real-time advisor logs.
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class AdvisorLogController {

    private final AdvisorLogPublisher advisorLogPublisher;

    @GetMapping(value = "/advisor-log", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter advisorLog() {
        return advisorLogPublisher.addEmitter();
    }
}

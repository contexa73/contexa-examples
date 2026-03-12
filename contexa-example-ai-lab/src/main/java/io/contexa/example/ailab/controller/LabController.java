package io.contexa.example.ailab.controller;

import io.contexa.example.ailab.domain.SentimentRequest;
import io.contexa.example.ailab.domain.SentimentResponse;
import io.contexa.example.ailab.lab.SentimentAnalysisLab;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller for sentiment analysis Lab operations.
 * Provides synchronous and SSE streaming endpoints.
 */
@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
public class LabController {

    private final SentimentAnalysisLab sentimentAnalysisLab;

    /**
     * Synchronous sentiment analysis.
     */
    @PostMapping("/analyze")
    public ResponseEntity<SentimentResponse> analyze(@RequestBody SentimentRequest request) {
        SentimentResponse response = sentimentAnalysisLab.process(request);
        return ResponseEntity.ok(response);
    }

    /**
     * SSE streaming sentiment analysis.
     * Streams intermediate chunks followed by ###FINAL_RESPONSE### marker and [DONE].
     */
    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> analyzeStream(@RequestBody SentimentRequest request) {
        return sentimentAnalysisLab.processStream(request)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}

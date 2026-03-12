package io.contexa.example.aillm.controller;

import io.contexa.contexacore.std.llm.client.ExecutionContext;
import io.contexa.contexacore.std.llm.client.UnifiedLLMOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST controller for direct UnifiedLLMOrchestrator usage.
 * Demonstrates ExecutionContext configuration and analysis levels.
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final UnifiedLLMOrchestrator orchestrator;

    /**
     * Synchronous chat via UnifiedLLMOrchestrator.execute().
     */
    @PostMapping("/chat")
    public Mono<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String level = request.getOrDefault("level", "NORMAL");

        ExecutionContext ctx = ExecutionContext.builder()
                .prompt(new Prompt(request.get("message")))
                .analysisLevel(ExecutionContext.AnalysisLevel.valueOf(level))
                .userId(auth.getName())
                .build();

        return orchestrator.execute(ctx)
                .map(result -> Map.of(
                        "response", (Object) result,
                        "level", level,
                        "user", auth.getName()
                ));
    }

    /**
     * SSE streaming chat via UnifiedLLMOrchestrator.stream().
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String level = request.getOrDefault("level", "NORMAL");

        ExecutionContext ctx = ExecutionContext.builder()
                .prompt(new Prompt(request.get("message")))
                .analysisLevel(ExecutionContext.AnalysisLevel.valueOf(level))
                .streamingMode(true)
                .userId(auth.getName())
                .build();

        return orchestrator.stream(ctx)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}

package io.contexa.example.aipipeline.controller;

import io.contexa.contexacommon.domain.TemplateType;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.pipeline.PipelineConfiguration;
import io.contexa.contexacore.std.pipeline.PipelineOrchestrator;
import io.contexa.example.aipipeline.domain.SecurityAnalysisContext;
import io.contexa.example.aipipeline.domain.SecurityAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST controller for pipeline-based code security analysis.
 * Demonstrates PipelineOrchestrator with all 6 pipeline steps.
 */
@RestController
@RequestMapping("/api/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineOrchestrator pipelineOrchestrator;

    /**
     * Synchronous pipeline execution with all 6 steps.
     */
    @PostMapping("/analyze")
    public Mono<ResponseEntity<SecurityAnalysisResponse>> analyze(@RequestBody Map<String, String> request) {
        AIRequest<SecurityAnalysisContext> aiRequest = buildAIRequest(request, "SECURITY_ANALYSIS");

        PipelineConfiguration config = PipelineConfiguration.builder()
                .addStep(PipelineConfiguration.PipelineStep.CONTEXT_RETRIEVAL)
                .addStep(PipelineConfiguration.PipelineStep.PREPROCESSING)
                .addStep(PipelineConfiguration.PipelineStep.PROMPT_GENERATION)
                .addStep(PipelineConfiguration.PipelineStep.LLM_EXECUTION)
                .addStep(PipelineConfiguration.PipelineStep.RESPONSE_PARSING)
                .addStep(PipelineConfiguration.PipelineStep.POSTPROCESSING)
                .build();

        return pipelineOrchestrator.execute(aiRequest, config, SecurityAnalysisResponse.class)
                .map(ResponseEntity::ok);
    }

    /**
     * SSE streaming pipeline execution.
     */
    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> analyzeStream(@RequestBody Map<String, String> request) {
        AIRequest<SecurityAnalysisContext> aiRequest = buildAIRequest(request, "SECURITY_ANALYSIS_STREAMING");

        PipelineConfiguration config = PipelineConfiguration.builder()
                .addStep(PipelineConfiguration.PipelineStep.CONTEXT_RETRIEVAL)
                .addStep(PipelineConfiguration.PipelineStep.PREPROCESSING)
                .addStep(PipelineConfiguration.PipelineStep.PROMPT_GENERATION)
                .addStep(PipelineConfiguration.PipelineStep.LLM_EXECUTION)
                .addStep(PipelineConfiguration.PipelineStep.RESPONSE_PARSING)
                .addStep(PipelineConfiguration.PipelineStep.POSTPROCESSING)
                .enableStreaming(true)
                .build();

        return pipelineOrchestrator.executeStream(aiRequest, config)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    private AIRequest<SecurityAnalysisContext> buildAIRequest(Map<String, String> request, String templateName) {
        SecurityAnalysisContext context = new SecurityAnalysisContext(
                request.get("code"),
                request.getOrDefault("language", "java")
        );
        return new AIRequest<>(
                context,
                new TemplateType(templateName),
                null
        );
    }
}

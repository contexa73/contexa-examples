package io.contexa.example.ailab.lab;

import io.contexa.contexacommon.domain.TemplateType;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.labs.AbstractAILab;
import io.contexa.contexacore.std.pipeline.PipelineConfiguration;
import io.contexa.contexacore.std.pipeline.PipelineOrchestrator;
import io.contexa.example.ailab.domain.SentimentContext;
import io.contexa.example.ailab.domain.SentimentRequest;
import io.contexa.example.ailab.domain.SentimentResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Custom AILab for sentiment analysis.
 * Demonstrates extending AbstractAILab with Pipeline integration.
 */
@Component
public class SentimentAnalysisLab extends AbstractAILab<SentimentRequest, SentimentResponse> {

    private final PipelineOrchestrator pipelineOrchestrator;

    public SentimentAnalysisLab(PipelineOrchestrator pipelineOrchestrator) {
        super("SentimentAnalysisLab");
        this.pipelineOrchestrator = pipelineOrchestrator;
    }

    @Override
    protected SentimentResponse doProcess(SentimentRequest request) {
        return doProcessAsync(request).block();
    }

    @Override
    protected Mono<SentimentResponse> doProcessAsync(SentimentRequest request) {
        AIRequest<SentimentContext> aiRequest = buildAIRequest(request, "SENTIMENT");

        PipelineConfiguration config = PipelineConfiguration.builder()
                .addStep(PipelineConfiguration.PipelineStep.CONTEXT_RETRIEVAL)
                .addStep(PipelineConfiguration.PipelineStep.PREPROCESSING)
                .addStep(PipelineConfiguration.PipelineStep.PROMPT_GENERATION)
                .addStep(PipelineConfiguration.PipelineStep.LLM_EXECUTION)
                .addStep(PipelineConfiguration.PipelineStep.RESPONSE_PARSING)
                .addStep(PipelineConfiguration.PipelineStep.POSTPROCESSING)
                .build();

        return pipelineOrchestrator.execute(aiRequest, config, SentimentResponse.class);
    }

    @Override
    protected Flux<String> doProcessStream(SentimentRequest request) {
        AIRequest<SentimentContext> aiRequest = buildAIRequest(request, "SENTIMENT_STREAMING");

        PipelineConfiguration config = PipelineConfiguration.builder()
                .addStep(PipelineConfiguration.PipelineStep.CONTEXT_RETRIEVAL)
                .addStep(PipelineConfiguration.PipelineStep.PREPROCESSING)
                .addStep(PipelineConfiguration.PipelineStep.PROMPT_GENERATION)
                .addStep(PipelineConfiguration.PipelineStep.LLM_EXECUTION)
                .addStep(PipelineConfiguration.PipelineStep.RESPONSE_PARSING)
                .addStep(PipelineConfiguration.PipelineStep.POSTPROCESSING)
                .enableStreaming(true)
                .build();

        return pipelineOrchestrator.executeStream(aiRequest, config);
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    private AIRequest<SentimentContext> buildAIRequest(SentimentRequest request, String templateName) {
        SentimentContext context = new SentimentContext(request.getText(), request.getLanguage());
        return new AIRequest<>(
                context,
                new TemplateType(templateName),
                null
        );
    }
}

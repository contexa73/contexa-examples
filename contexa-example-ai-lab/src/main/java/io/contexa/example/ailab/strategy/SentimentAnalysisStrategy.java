package io.contexa.example.ailab.strategy;

import io.contexa.contexacommon.domain.DiagnosisType;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.labs.AILabFactory;
import io.contexa.contexacore.std.strategy.AbstractAIStrategy;
import io.contexa.example.ailab.domain.SentimentContext;
import io.contexa.example.ailab.domain.SentimentRequest;
import io.contexa.example.ailab.domain.SentimentResponse;
import io.contexa.example.ailab.lab.SentimentAnalysisLab;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Strategy for sentiment analysis.
 * Routes SENTIMENT DiagnosisType requests to SentimentAnalysisLab.
 */
@Component
public class SentimentAnalysisStrategy extends AbstractAIStrategy<SentimentContext, SentimentResponse> {

    public SentimentAnalysisStrategy(AILabFactory labFactory) {
        super(labFactory);
    }

    @Override
    public DiagnosisType getSupportedType() {
        return new DiagnosisType("SENTIMENT");
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    protected void validateRequest(AIRequest<SentimentContext> request) {
        if (request.getContext() == null || request.getContext().getText() == null) {
            throw new IllegalArgumentException("SentimentContext with text is required");
        }
    }

    @Override
    protected Class<?> getLabType() {
        return SentimentAnalysisLab.class;
    }

    @Override
    protected Object convertLabRequest(AIRequest<SentimentContext> request) {
        SentimentContext ctx = request.getContext();
        return new SentimentRequest(ctx.getText(), ctx.getLanguage());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SentimentResponse processLabExecution(Object lab, Object labRequest, AIRequest<SentimentContext> request) throws Exception {
        SentimentAnalysisLab sentimentLab = (SentimentAnalysisLab) lab;
        SentimentRequest sentimentRequest = (SentimentRequest) labRequest;
        return sentimentLab.process(sentimentRequest);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Mono<SentimentResponse> processLabExecutionAsync(Object lab, Object labRequest, AIRequest<SentimentContext> originRequest) {
        SentimentAnalysisLab sentimentLab = (SentimentAnalysisLab) lab;
        SentimentRequest sentimentRequest = (SentimentRequest) labRequest;
        return sentimentLab.processAsync(sentimentRequest);
    }
}

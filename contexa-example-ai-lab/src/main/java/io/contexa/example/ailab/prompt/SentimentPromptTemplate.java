package io.contexa.example.ailab.prompt;

import io.contexa.contexacommon.domain.TemplateType;
import io.contexa.contexacommon.domain.context.DomainContext;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.components.prompt.AbstractStandardPromptTemplate;
import io.contexa.example.ailab.domain.SentimentContext;
import io.contexa.example.ailab.domain.SentimentResponse;
import org.springframework.stereotype.Component;

/**
 * Standard prompt template for sentiment analysis.
 * Extends AbstractStandardPromptTemplate with BeanOutputConverter for typed JSON parsing.
 */
@Component
public class SentimentPromptTemplate extends AbstractStandardPromptTemplate<SentimentResponse> {

    public SentimentPromptTemplate() {
        super(SentimentResponse.class);
    }

    @Override
    protected String generateDomainSystemPrompt(AIRequest<? extends DomainContext> request, String systemMetadata) {
        return """
                You are a sentiment analysis AI. Analyze the given text and return a structured JSON response.
                Identify the overall sentiment (POSITIVE, NEGATIVE, NEUTRAL, MIXED), confidence score (0.0-1.0),
                extract key emotion-bearing keywords, and provide a brief summary.
                Always respond in valid JSON format.""";
    }

    @Override
    public String generateUserPrompt(AIRequest<? extends DomainContext> request, String contextInfo) {
        SentimentContext ctx = (SentimentContext) request.getContext();
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the sentiment of the following text");
        if (ctx.getLanguage() != null && !"en".equals(ctx.getLanguage())) {
            prompt.append(" (language: ").append(ctx.getLanguage()).append(")");
        }
        prompt.append(":\n\n").append(ctx.getText());
        if (contextInfo != null && !contextInfo.isBlank()) {
            prompt.append("\n\nAdditional context:\n").append(contextInfo);
        }
        return prompt.toString();
    }

    @Override
    public TemplateType getSupportedType() {
        return new TemplateType("SENTIMENT");
    }
}

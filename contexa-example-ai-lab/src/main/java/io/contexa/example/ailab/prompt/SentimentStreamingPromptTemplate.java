package io.contexa.example.ailab.prompt;

import io.contexa.contexacommon.domain.TemplateType;
import io.contexa.contexacommon.domain.context.DomainContext;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.components.prompt.AbstractStreamingPromptTemplate;
import io.contexa.example.ailab.domain.SentimentContext;
import org.springframework.stereotype.Component;

/**
 * Streaming prompt template for sentiment analysis.
 * Used when the client requests SSE streaming responses.
 */
@Component
public class SentimentStreamingPromptTemplate extends AbstractStreamingPromptTemplate {

    @Override
    protected String generateDomainSystemPrompt(AIRequest<? extends DomainContext> request, String systemMetadata) {
        return """
                You are a sentiment analysis AI. Analyze the given text and return a structured JSON response.
                Identify the overall sentiment (POSITIVE, NEGATIVE, NEUTRAL, MIXED), confidence score (0.0-1.0),
                extract key emotion-bearing keywords, and provide a brief summary.""";
    }

    @Override
    protected String getJsonSchemaExample() {
        return """
                {
                  "sentiment": "POSITIVE",
                  "confidence": 0.92,
                  "keywords": ["excellent", "impressive", "recommend"],
                  "summary": "The text expresses a strongly positive sentiment."
                }""";
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
        return new TemplateType("SENTIMENT_STREAMING");
    }
}

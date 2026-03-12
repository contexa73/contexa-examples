package io.contexa.example.ailab.domain;

import io.contexa.contexacommon.domain.context.DomainContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Custom DomainContext for sentiment analysis.
 * Demonstrates how to extend DomainContext with domain-specific fields.
 */
@Getter
@Setter
public class SentimentContext extends DomainContext {

    private String text;
    private String language;

    public SentimentContext() {
        super();
    }

    public SentimentContext(String text, String language) {
        super();
        this.text = text;
        this.language = language != null ? language : "en";
    }

    @Override
    public String getDomainType() {
        return "SENTIMENT";
    }
}

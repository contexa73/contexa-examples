package io.contexa.example.ailab.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.contexa.contexacommon.domain.request.AIResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Custom AIResponse for sentiment analysis results.
 * Demonstrates how to extend AIResponse with domain-specific fields.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentimentResponse extends AIResponse {

    private String sentiment;
    private double confidence;
    private List<String> keywords;
    private String summary;
}

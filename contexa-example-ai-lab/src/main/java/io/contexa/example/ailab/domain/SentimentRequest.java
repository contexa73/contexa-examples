package io.contexa.example.ailab.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lab request DTO for sentiment analysis.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {

    private String text;
    private String language;
}

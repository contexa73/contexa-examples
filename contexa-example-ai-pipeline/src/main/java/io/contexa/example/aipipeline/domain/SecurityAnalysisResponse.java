package io.contexa.example.aipipeline.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.contexa.contexacommon.domain.request.AIResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Custom AIResponse for code security analysis results.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAnalysisResponse extends AIResponse {

    private List<Vulnerability> vulnerabilities;
    private String overallSeverity;
    private List<String> recommendations;
    private String summary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vulnerability {
        private String type;
        private String severity;
        private String description;
        private String location;
        private String fix;
    }
}

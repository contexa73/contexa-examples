package io.contexa.example.aipipeline.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.contexa.contexacore.std.pipeline.PipelineExecutionContext;
import io.contexa.contexacore.std.pipeline.processor.DomainResponseProcessor;
import io.contexa.example.aipipeline.domain.SecurityAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Custom DomainResponseProcessor for the POSTPROCESSING pipeline step.
 * Converts raw LLM JSON output into SecurityAnalysisResponse.
 */
@Slf4j
@Component
public class SecurityResponseProcessor implements DomainResponseProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String templateKey) {
        return "SECURITY_ANALYSIS".equals(templateKey);
    }

    @Override
    public boolean supportsType(Class<?> responseType) {
        return SecurityAnalysisResponse.class.isAssignableFrom(responseType);
    }

    @Override
    public Object wrapResponse(Object parsedData, PipelineExecutionContext context) {
        if (parsedData instanceof SecurityAnalysisResponse) {
            return parsedData;
        }
        try {
            String json = parsedData instanceof String
                    ? (String) parsedData
                    : objectMapper.writeValueAsString(parsedData);
            return objectMapper.readValue(json, SecurityAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to wrap response into SecurityAnalysisResponse", e);
            SecurityAnalysisResponse fallback = new SecurityAnalysisResponse();
            fallback.setSummary("Failed to parse analysis result: " + e.getMessage());
            return fallback;
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

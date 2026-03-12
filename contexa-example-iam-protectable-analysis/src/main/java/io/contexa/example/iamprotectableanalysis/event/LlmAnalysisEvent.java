package io.contexa.example.iamprotectableanalysis.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM analysis event domain model for SSE streaming.
 *
 * Event flow:
 * 1. CONTEXT_COLLECTED - Context collection complete
 * 2. LAYER1_START      - Layer1 analysis start
 * 3. LAYER1_COMPLETE   - Layer1 analysis complete
 * 4. LAYER2_START      - Layer2 escalation (optional)
 * 5. LAYER2_COMPLETE   - Layer2 analysis complete (optional)
 * 6. DECISION_APPLIED  - Final decision applied
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class LlmAnalysisEvent {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty("type")
    private String type;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("layer")
    private String layer;

    @JsonProperty("status")
    private String status;

    @JsonProperty("action")
    private String action;

    @JsonProperty("riskScore")
    private Double riskScore;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("reasoning")
    private String reasoning;

    @JsonProperty("mitre")
    private String mitre;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("elapsedMs")
    private Long elapsedMs;

    @JsonProperty("requestPath")
    private String requestPath;

    @JsonProperty("analysisRequirement")
    private String analysisRequirement;

    @JsonProperty("metadata")
    private String metadata;

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("[LlmAnalysisEvent] JSON serialization failed", e);
            return "{}";
        }
    }

    public static class EventType {
        public static final String CONTEXT_COLLECTED = "CONTEXT_COLLECTED";
        public static final String LAYER1_START = "LAYER1_START";
        public static final String LAYER1_COMPLETE = "LAYER1_COMPLETE";
        public static final String LAYER2_START = "LAYER2_START";
        public static final String LAYER2_COMPLETE = "LAYER2_COMPLETE";
        public static final String DECISION_APPLIED = "DECISION_APPLIED";
        public static final String ERROR = "ERROR";
    }

    public static class Status {
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String COMPLETED = "COMPLETED";
        public static final String ESCALATED = "ESCALATED";
        public static final String ERROR = "ERROR";
    }

    public static class Layer {
        public static final String LAYER1 = "LAYER1";
        public static final String LAYER2 = "LAYER2";
    }

    public static LlmAnalysisEvent contextCollected(String userId, String requestPath, String analysisRequirement) {
        return LlmAnalysisEvent.builder().type(EventType.CONTEXT_COLLECTED).userId(userId)
                .requestPath(requestPath).analysisRequirement(analysisRequirement)
                .status(Status.COMPLETED).timestamp(System.currentTimeMillis()).build();
    }

    public static LlmAnalysisEvent layer1Start(String userId, String requestPath) {
        return LlmAnalysisEvent.builder().type(EventType.LAYER1_START).userId(userId)
                .requestPath(requestPath).layer(Layer.LAYER1)
                .status(Status.IN_PROGRESS).timestamp(System.currentTimeMillis()).build();
    }

    public static LlmAnalysisEvent layer1Complete(String userId, String action,
            Double riskScore, Double confidence, String reasoning, String mitre, Long elapsedMs) {
        return LlmAnalysisEvent.builder().type(EventType.LAYER1_COMPLETE).userId(userId)
                .layer(Layer.LAYER1).status(Status.COMPLETED).action(action)
                .riskScore(riskScore).confidence(confidence).reasoning(reasoning)
                .mitre(mitre).elapsedMs(elapsedMs).timestamp(System.currentTimeMillis()).build();
    }

    public static LlmAnalysisEvent layer2Start(String userId, String requestPath, String reason) {
        return LlmAnalysisEvent.builder().type(EventType.LAYER2_START).userId(userId)
                .requestPath(requestPath).layer(Layer.LAYER2)
                .status(Status.IN_PROGRESS).reasoning(reason).timestamp(System.currentTimeMillis()).build();
    }

    public static LlmAnalysisEvent layer2Complete(String userId, String action,
            Double riskScore, Double confidence, String reasoning, String mitre, Long elapsedMs) {
        return LlmAnalysisEvent.builder().type(EventType.LAYER2_COMPLETE).userId(userId)
                .layer(Layer.LAYER2).status(Status.COMPLETED).action(action)
                .riskScore(riskScore).confidence(confidence).reasoning(reasoning)
                .mitre(mitre).elapsedMs(elapsedMs).timestamp(System.currentTimeMillis()).build();
    }

    public static LlmAnalysisEvent decisionApplied(String userId, String action, String layer, String requestPath) {
        return LlmAnalysisEvent.builder().type(EventType.DECISION_APPLIED).userId(userId)
                .action(action).layer(layer).requestPath(requestPath)
                .status(Status.COMPLETED).timestamp(System.currentTimeMillis()).build();
    }

    public static LlmAnalysisEvent error(String userId, String message) {
        return LlmAnalysisEvent.builder().type(EventType.ERROR).userId(userId)
                .status(Status.ERROR).reasoning(message).timestamp(System.currentTimeMillis()).build();
    }
}

package io.contexa.example.aipipeline.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event representing pipeline step progress for SSE broadcasting.
 */
@Getter
@AllArgsConstructor
public class PipelineProgressEvent {

    private final String stepName;
    private final String status;
    private final long elapsedMs;
    private final String message;
}

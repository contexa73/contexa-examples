package io.contexa.example.aillm.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event representing advisor lifecycle action for SSE broadcasting.
 */
@Getter
@AllArgsConstructor
public class AdvisorLogEvent {

    private final String advisorName;
    private final String phase;
    private final String data;
    private final long timestamp;
}

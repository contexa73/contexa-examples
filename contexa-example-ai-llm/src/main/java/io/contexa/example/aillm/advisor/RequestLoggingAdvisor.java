package io.contexa.example.aillm.advisor;

import io.contexa.contexacore.std.advisor.core.BaseAdvisor;
import io.contexa.example.aillm.event.AdvisorLogPublisher;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.stereotype.Component;

/**
 * Custom Advisor that logs request/response lifecycle.
 * Publishes events via SSE for real-time UI display.
 */
@Component
public class RequestLoggingAdvisor extends BaseAdvisor {

    private final AdvisorLogPublisher logPublisher;

    public RequestLoggingAdvisor(AdvisorLogPublisher logPublisher) {
        super("llm", "request-logging", 100);
        this.logPublisher = logPublisher;
    }

    @Override
    protected ChatClientRequest beforeCall(ChatClientRequest request) {
        String promptPreview = request.prompt() != null
                ? request.prompt().toString().substring(0, Math.min(200, request.prompt().toString().length()))
                : "N/A";
        logPublisher.publish("RequestLogging", "BEFORE_CALL",
                "Prompt: " + promptPreview);
        return request;
    }

    @Override
    protected ChatClientResponse afterCall(ChatClientResponse response, ChatClientRequest request) {
        String responsePreview = response.chatResponse() != null
                ? response.chatResponse().getResult().getOutput().getText()
                : "N/A";
        if (responsePreview != null && responsePreview.length() > 200) {
            responsePreview = responsePreview.substring(0, 200) + "...";
        }
        logPublisher.publish("RequestLogging", "AFTER_CALL",
                "Response length: " + (responsePreview != null ? responsePreview.length() : 0));
        return response;
    }
}

package io.contexa.example.aillm.advisor;

import io.contexa.contexacore.std.advisor.core.BaseAdvisor;
import io.contexa.example.aillm.event.AdvisorLogPublisher;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.stereotype.Component;

/**
 * Custom Advisor that validates LLM responses.
 * Checks response length and content quality.
 */
@Component
public class ResponseValidationAdvisor extends BaseAdvisor {

    private final AdvisorLogPublisher logPublisher;

    public ResponseValidationAdvisor(AdvisorLogPublisher logPublisher) {
        super("llm", "response-validation", 200);
        this.logPublisher = logPublisher;
    }

    @Override
    protected ChatClientRequest beforeCall(ChatClientRequest request) {
        logPublisher.publish("ResponseValidation", "BEFORE_CALL", "Request passed validation");
        return request;
    }

    @Override
    protected ChatClientResponse afterCall(ChatClientResponse response, ChatClientRequest request) {
        String text = response.chatResponse() != null
                ? response.chatResponse().getResult().getOutput().getText()
                : null;

        boolean valid = text != null && !text.isBlank() && text.length() > 10;
        logPublisher.publish("ResponseValidation", "AFTER_CALL",
                "Valid: " + valid + ", Length: " + (text != null ? text.length() : 0));

        return response;
    }
}

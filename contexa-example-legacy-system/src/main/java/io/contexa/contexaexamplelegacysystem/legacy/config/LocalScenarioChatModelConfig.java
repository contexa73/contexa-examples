package io.contexa.contexaexamplelegacysystem.legacy.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@Profile("fake-chat")
public class LocalScenarioChatModelConfig {

    @Bean
    @Primary
    public ChatModel standardChatModel() {
        return new DeterministicScenarioChatModel();
    }

    @Bean
    @Primary
    public VectorStore scenarioVectorStore() {
        return new NoOpScenarioVectorStore();
    }

    private static final class DeterministicScenarioChatModel implements ChatModel {

        @Override
        public ChatResponse call(Prompt prompt) {
            AssistantMessage message = new AssistantMessage("""
                    {"decision":"PERMIT","riskScore":0,"reason":"local scenario fixture"}
                    """);
            return new ChatResponse(List.of(new Generation(message)));
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return ChatOptions.builder()
                    .model("contexa-local-scenario")
                    .temperature(0.0)
                    .build();
        }
    }

    private static final class NoOpScenarioVectorStore implements VectorStore {

        @Override
        public void add(List<Document> documents) {
        }

        @Override
        public void delete(List<String> ids) {
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return List.of();
        }
    }
}

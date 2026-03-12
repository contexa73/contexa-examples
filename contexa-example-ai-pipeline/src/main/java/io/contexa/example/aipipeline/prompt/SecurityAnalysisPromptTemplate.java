package io.contexa.example.aipipeline.prompt;

import io.contexa.contexacommon.domain.TemplateType;
import io.contexa.contexacommon.domain.context.DomainContext;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.components.prompt.AbstractStandardPromptTemplate;
import io.contexa.example.aipipeline.domain.SecurityAnalysisContext;
import io.contexa.example.aipipeline.domain.SecurityAnalysisResponse;
import org.springframework.stereotype.Component;

/**
 * Standard prompt template for code security analysis.
 * Used in the PROMPT_GENERATION pipeline step.
 */
@Component
public class SecurityAnalysisPromptTemplate extends AbstractStandardPromptTemplate<SecurityAnalysisResponse> {

    public SecurityAnalysisPromptTemplate() {
        super(SecurityAnalysisResponse.class);
    }

    @Override
    protected String generateDomainSystemPrompt(AIRequest<? extends DomainContext> request, String systemMetadata) {
        return """
                You are a code security analyzer. Identify vulnerabilities in the given code snippet.
                Check for common issues: SQL injection, XSS, command injection, path traversal,
                insecure deserialization, hardcoded credentials, and other OWASP Top 10 vulnerabilities.
                Rate each vulnerability severity as CRITICAL, HIGH, MEDIUM, or LOW.
                Provide specific fix recommendations for each finding.
                Always respond in valid JSON format.""";
    }

    @Override
    public String generateUserPrompt(AIRequest<? extends DomainContext> request, String contextInfo) {
        SecurityAnalysisContext ctx = (SecurityAnalysisContext) request.getContext();
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following ").append(ctx.getLanguage()).append(" code for security vulnerabilities:\n\n");
        prompt.append("```").append(ctx.getLanguage()).append("\n");
        prompt.append(ctx.getCodeSnippet());
        prompt.append("\n```");
        if (contextInfo != null && !contextInfo.isBlank()) {
            prompt.append("\n\nKnowledge base context:\n").append(contextInfo);
        }
        return prompt.toString();
    }

    @Override
    public TemplateType getSupportedType() {
        return new TemplateType("SECURITY_ANALYSIS");
    }
}

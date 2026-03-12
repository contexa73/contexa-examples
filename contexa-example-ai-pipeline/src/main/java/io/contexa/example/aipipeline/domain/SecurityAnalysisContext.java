package io.contexa.example.aipipeline.domain;

import io.contexa.contexacommon.domain.context.DomainContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Custom DomainContext for code security analysis.
 */
@Getter
@Setter
public class SecurityAnalysisContext extends DomainContext {

    private String codeSnippet;
    private String language;

    public SecurityAnalysisContext() {
        super();
    }

    public SecurityAnalysisContext(String codeSnippet, String language) {
        super();
        this.codeSnippet = codeSnippet;
        this.language = language != null ? language : "java";
    }

    @Override
    public String getDomainType() {
        return "CODE_SECURITY";
    }
}

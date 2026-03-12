package io.contexa.example.aipipeline.step;

import io.contexa.contexacommon.domain.context.DomainContext;
import io.contexa.contexacommon.domain.request.AIRequest;
import io.contexa.contexacore.std.pipeline.PipelineConfiguration;
import io.contexa.contexacore.std.pipeline.PipelineExecutionContext;
import io.contexa.contexacore.std.pipeline.step.PipelineStep;
import io.contexa.example.aipipeline.domain.SecurityAnalysisContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Custom PipelineStep that normalizes code before analysis.
 * Runs in PREPROCESSING phase at order=50 (before default PreprocessingStep at order=100).
 */
@Component
public class CodeNormalizationStep implements PipelineStep {

    @Override
    public <T extends DomainContext> Mono<Object> execute(AIRequest<T> request, PipelineExecutionContext context) {
        return Mono.fromCallable(() -> {
            SecurityAnalysisContext ctx = (SecurityAnalysisContext) request.getContext();
            String code = ctx.getCodeSnippet();

            // Normalize: trim trailing whitespace, normalize line endings
            String normalized = code
                    .replaceAll("[ \t]+$", "")
                    .replace("\r\n", "\n")
                    .replace("\r", "\n");

            ctx.setCodeSnippet(normalized);

            // Store metadata for downstream steps
            context.addMetadata("codeLines", normalized.split("\n").length);
            context.addMetadata("codeLanguage", ctx.getLanguage());
            context.addMetadata("codeNormalized", true);

            return normalized;
        });
    }

    @Override
    public PipelineConfiguration.PipelineStep getConfigStep() {
        return PipelineConfiguration.PipelineStep.PREPROCESSING;
    }

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public String getStepName() {
        return "CodeNormalization";
    }
}

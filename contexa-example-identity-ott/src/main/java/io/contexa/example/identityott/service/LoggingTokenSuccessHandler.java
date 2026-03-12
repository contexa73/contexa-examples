package io.contexa.example.identityott.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logs the generated OTT token to console instead of sending email.
 * In production, replace with an email/SMS-based handler.
 */
@Slf4j
@Component
public class LoggingTokenSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

    private final RedirectOneTimeTokenGenerationSuccessHandler delegate =
            new RedirectOneTimeTokenGenerationSuccessHandler("/ott/verify");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
            throws IOException {
        log.error("[OTT] ====================================");
        log.error("[OTT] Token for user [{}]: {}", oneTimeToken.getUsername(), oneTimeToken.getTokenValue());
        log.error("[OTT] ====================================");

        delegate.handle(request, response, oneTimeToken);
    }
}

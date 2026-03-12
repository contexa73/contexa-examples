package io.contexa.example.identityott.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OneTimeTokenService for demonstration.
 * Generates 6-digit numeric tokens with 5-minute TTL.
 * In production, use JdbcOneTimeTokenService or a Redis-backed implementation.
 */
@Slf4j
@Service
public class InMemoryOneTimeTokenService implements OneTimeTokenService {

    private static final long TOKEN_TTL_SECONDS = 300;

    private final Map<String, StoredToken> tokenStore = new ConcurrentHashMap<>();

    @Override
    public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
        String tokenValue = generateNumericToken();
        Instant expiresAt = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);

        tokenStore.put(tokenValue, new StoredToken(request.getUsername(), expiresAt));

        log.error("[OTT] Token generated for user: {}, token: {}", request.getUsername(), tokenValue);

        return new DefaultOneTimeToken(tokenValue, request.getUsername(), expiresAt);
    }

    @Override
    public OneTimeToken consume(OneTimeTokenAuthenticationToken token) {
        String tokenValue = token.getTokenValue();
        StoredToken stored = tokenStore.remove(tokenValue);

        if (stored == null) {
            return null;
        }

        if (stored.expiresAt().isBefore(Instant.now())) {
            return null;
        }

        return new DefaultOneTimeToken(tokenValue, stored.username(), stored.expiresAt());
    }

    private String generateNumericToken() {
        int code = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(code);
    }

    private record StoredToken(String username, Instant expiresAt) {}
}

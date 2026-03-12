package io.contexa.example.identityott.config;

import io.contexa.contexacore.security.AISessionSecurityContextRepository;
import io.contexa.contexaiam.security.xacml.pep.CustomDynamicAuthorizationManager;
import io.contexa.contexaidentity.security.core.config.PlatformConfig;
import io.contexa.contexaidentity.security.core.dsl.IdentityDslRegistry;
import io.contexa.contexaidentity.security.core.dsl.common.SafeHttpCustomizer;
import io.contexa.example.identityott.service.InMemoryOneTimeTokenService;
import io.contexa.example.identityott.service.LoggingTokenSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * OTT (One-Time Token / Magic Link) authentication example.
 *
 * Uses registry.ott() for passwordless login.
 * Token is generated and logged to console (simulating email delivery).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomDynamicAuthorizationManager customDynamicAuthorizationManager;
    private final AISessionSecurityContextRepository aiSessionSecurityContextRepository;
    private final InMemoryOneTimeTokenService inMemoryOneTimeTokenService;
    private final LoggingTokenSuccessHandler loggingTokenSuccessHandler;

    @Bean
    public PlatformConfig platformDslConfig(IdentityDslRegistry<HttpSecurity> registry) throws Exception {

        SafeHttpCustomizer<HttpSecurity> globalHttpCustomizer = http -> {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authReq -> authReq
                            .requestMatchers("/ott/**", "/api/health").permitAll()
                            .anyRequest().access(customDynamicAuthorizationManager)
                    )
                    .securityContext(sc ->
                            sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)
                .ott(ott -> ott
                        .tokenGeneratingUrl("/ott/generate-token")
                        .defaultSubmitPageUrl("/ott/verify")
                        .tokenService(inMemoryOneTimeTokenService)
                        .tokenGenerationSuccessHandler(loggingTokenSuccessHandler))
                .session(Customizer.withDefaults())
                .build();
    }
}

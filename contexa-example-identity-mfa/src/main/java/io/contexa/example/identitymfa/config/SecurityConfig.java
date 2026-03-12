package io.contexa.example.identitymfa.config;

import io.contexa.contexacore.security.AISessionSecurityContextRepository;
import io.contexa.contexaiam.security.xacml.pep.CustomDynamicAuthorizationManager;
import io.contexa.contexaidentity.security.core.config.PlatformConfig;
import io.contexa.contexaidentity.security.core.dsl.IdentityDslRegistry;
import io.contexa.contexaidentity.security.core.dsl.common.SafeHttpCustomizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Advanced MFA example with custom pages.
 *
 * Demonstrates:
 * - Custom login page (/customLogin)
 * - OTT + Passkey as secondary factors
 * - Custom MFA page URLs via mfaPage(...)
 * - ContexaMFA JavaScript SDK integration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomDynamicAuthorizationManager customDynamicAuthorizationManager;
    private final AISessionSecurityContextRepository aiSessionSecurityContextRepository;

    @Bean
    public PlatformConfig platformDslConfig(IdentityDslRegistry<HttpSecurity> registry) throws Exception {

        SafeHttpCustomizer<HttpSecurity> globalHttpCustomizer = http -> {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authReq -> authReq
                            .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                            .requestMatchers("/customLogin", "/api/health").permitAll()
                            .anyRequest().access(customDynamicAuthorizationManager)
                    )
                    .securityContext(sc ->
                            sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)
                .mfa(mfa -> mfa
                        .primaryAuthentication(auth -> auth
                                .formLogin(form -> form
                                        .loginPage("/customLogin")
                                        .defaultSuccessUrl("/dashboard")))
                        .ott(Customizer.withDefaults())
                        .passkey(Customizer.withDefaults())
                        .mfaPage(page -> page
                                .ottPages("/custom/mfa/ott/request-code-ui", "/custom/mfa/challenge/ott")
                                .passkeyChallengePages("/custom/challenge/passkey")))
                .session(Customizer.withDefaults())
                .build();
    }
}

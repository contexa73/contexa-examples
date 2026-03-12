package io.contexa.example.identitydsl.config;

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
 * Custom PlatformSecurityConfig using the Identity DSL.
 *
 * This replaces the default PlatformConfig created by @EnableAISecurity.
 * Define your own bean only when you need to customize:
 *   - Authentication flows (form login, REST, MFA, passkey)
 *   - Authorization rules (static resource exclusions, public endpoints)
 *   - Session management
 *
 * Three integration pillars:
 *   1. Identity DSL       — authentication flow composition via IdentityDslRegistry
 *   2. Dynamic Authorization — AI-driven access control via CustomDynamicAuthorizationManager
 *   3. AI Session Context  — continuous trust assessment via AISessionSecurityContextRepository
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomDynamicAuthorizationManager customDynamicAuthorizationManager;
    private final AISessionSecurityContextRepository aiSessionSecurityContextRepository;

    @Bean
    public PlatformConfig platformDslConfig(IdentityDslRegistry<HttpSecurity> registry) throws Exception {

        // Global HTTP customizer: applies to ALL filter chains
        SafeHttpCustomizer<HttpSecurity> globalHttpCustomizer = http -> {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authReq -> authReq
                    // Static resources — excluded from AI security evaluation
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    // Public endpoints
                    .requestMatchers("/login", "/api/health").permitAll()
                    // Everything else — evaluated by AI-driven authorization
                    .anyRequest().access(customDynamicAuthorizationManager)
                )
                // AI session context — augments sessions with trust scores and threat assessments
                .securityContext(sc ->
                    sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)
                // MFA with form login as primary + passkey as secondary
                .mfa(mfa -> mfa
                    .primaryAuthentication(auth -> auth
                        .formLogin(form -> form
                            .loginPage("/login")
                            .defaultSuccessUrl("/dashboard")))
                    .passkey(Customizer.withDefaults())
                )
                .session(Customizer.withDefaults())
                .build();
    }
}

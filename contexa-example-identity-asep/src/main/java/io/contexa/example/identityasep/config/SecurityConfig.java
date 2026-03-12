package io.contexa.example.identityasep.config;

import io.contexa.contexacore.security.AISessionSecurityContextRepository;
import io.contexa.contexaiam.security.xacml.pep.CustomDynamicAuthorizationManager;
import io.contexa.contexaidentity.security.core.config.PlatformConfig;
import io.contexa.contexaidentity.security.core.dsl.IdentityDslRegistry;
import io.contexa.contexaidentity.security.core.dsl.common.SafeHttpCustomizer;
import io.contexa.example.identityasep.filter.CustomSecurityExceptionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ASEP example SecurityConfig.
 *
 * Registers a custom filter (CustomSecurityExceptionFilter) that throws exceptions
 * within the security filter chain. ASEPFilter catches these exceptions and
 * delegates to @SecurityControllerAdvice (CustomSecurityExceptionAdvice).
 *
 * Flow: Request -> Filter Chain -> CustomSecurityExceptionFilter (throws)
 *       -> ASEPFilter (catches) -> @SecurityControllerAdvice (handles) -> JSON Response
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
                    .requestMatchers("/login", "/test/**", "/api/auth/**", "/api/health").permitAll()
                    .requestMatchers("/api/trigger-*").permitAll()
                    .anyRequest().access(customDynamicAuthorizationManager)
                )
                .securityContext(sc ->
                    sc.securityContextRepository(aiSessionSecurityContextRepository))
                // Register custom filter that throws exceptions for demo purposes
                .addFilterBefore(new CustomSecurityExceptionFilter(),
                    UsernamePasswordAuthenticationFilter.class);
        };

        return registry
                .global(globalHttpCustomizer)
                .form(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/test/asep")
                    .asep(Customizer.withDefaults()))
                .session(Customizer.withDefaults())
                .build();
    }
}

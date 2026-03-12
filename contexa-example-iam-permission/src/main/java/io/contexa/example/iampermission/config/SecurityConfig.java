package io.contexa.example.iampermission.config;

import io.contexa.contexacore.security.AISessionSecurityContextRepository;
import io.contexa.contexaiam.security.xacml.pep.CustomDynamicAuthorizationManager;
import io.contexa.contexaidentity.security.core.config.PlatformConfig;
import io.contexa.contexaidentity.security.core.dsl.IdentityDslRegistry;
import io.contexa.contexaidentity.security.core.dsl.common.SafeHttpCustomizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Permission evaluator security configuration.
 *
 * EnableMethodSecurity activates @PreAuthorize("hasPermission(...)") support.
 * CompositePermissionEvaluator automatically routes to registered DomainPermissionEvaluator beans.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                            .requestMatchers("/api/auth/**", "/api/health").permitAll()
                            .anyRequest().access(customDynamicAuthorizationManager))
                    .securityContext(sc ->
                            sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)
                .form(form -> form
                        .defaultSuccessUrl("/api/profile"))
                .session(Customizer.withDefaults())
                .build();
    }
}

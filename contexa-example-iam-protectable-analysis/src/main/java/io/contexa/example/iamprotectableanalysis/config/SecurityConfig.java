package io.contexa.example.iamprotectableanalysis.config;

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
 * @Protectable analysis example with SSE real-time visualization.
 *
 * Permits SSE and test-action endpoints for the security test UI.
 * All other requests go through dynamic authorization.
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
                            .requestMatchers("/css/**", "/js/**", "/api/auth/**",
                                    "/test/**", "/api/sse/**", "/api/test-action/**").permitAll()
                            .anyRequest().access(customDynamicAuthorizationManager))
                    .securityContext(sc ->
                            sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)
                .form(form -> form
                        .defaultSuccessUrl("/test/security"))
                .session(Customizer.withDefaults())
                .build();
    }
}

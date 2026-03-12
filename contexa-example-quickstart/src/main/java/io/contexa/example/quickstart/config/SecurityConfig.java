package io.contexa.example.quickstart.config;

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
 * SecurityConfig for quickstart test page access.
 *
 * Overrides the default PlatformConfig to add permitAll for
 * static resources and the test page endpoint.
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
                    .requestMatchers("/login", "/test/**", "/api/health").permitAll()
                    .anyRequest().access(customDynamicAuthorizationManager)
                )
                .securityContext(sc ->
                    sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)
                .form(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/test/quickstart"))
                .session(Customizer.withDefaults())
                .build();
    }
}

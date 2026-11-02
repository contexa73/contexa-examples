package io.contexa.contexaexamplelegacysystem.legacy.config;

import io.contexa.contexaexamplelegacysystem.legacy.filter.LegacySessionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * The application's own Spring Security chain.
 * <p>
 * In SANDBOX mode the application owns sign-in (HOST_OWNED), so Contexa builds no chain. Without this bean Spring
 * Boot's default security would require a login for every request. The chain keeps the legacy access rules (the
 * legacy filter and interceptor still decide) and puts the legacy session user into the SecurityContext, so that
 * Contexa's bridge and @Protectable see an authenticated user.
 */
@Configuration
public class LegacySecurityConfig {

    @Bean
    public SecurityFilterChain legacyChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new LegacySessionAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .build();
    }
}

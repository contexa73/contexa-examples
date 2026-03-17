package io.contexa.contexaexampleidentitymfamulti.config;

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
 * Multi MFA example demonstrating two independent MFA flows.
 *
 * Flow 1 (Admin): urlPrefix("/admin") + Passkey
 *   - /admin/mfa/login -> auto-generated login form
 *   - /admin/mfa/select-factor -> factor selection
 *   - /admin/mfa/challenge/passkey -> passkey challenge
 *   - securityMatcher("/admin/**") auto-configured
 *
 * Flow 2 (User): urlPrefix("/user") + OTT
 *   - /user/mfa/login -> auto-generated login form
 *   - /user/mfa/select-factor -> factor selection
 *   - /user/mfa/challenge/ott -> OTT challenge
 *   - securityMatcher("/user/**") auto-configured
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
                            .requestMatchers("/", "/api/health").permitAll()
                            .anyRequest().access(customDynamicAuthorizationManager)
                    )
                    .securityContext(sc ->
                            sc.securityContextRepository(aiSessionSecurityContextRepository));
        };

        return registry
                .global(globalHttpCustomizer)

                // Admin MFA Flow: Passkey only
                .mfa(mfa -> mfa
                        .name("admin")
                        .urlPrefix("/admin")
                        .primaryAuthentication(auth -> auth
                                .formLogin(form -> form
                                        .defaultSuccessUrl("/admin/dashboard")))
                        .passkey(Customizer.withDefaults())
                        .order(60)
                ).session(Customizer.withDefaults())

                // User MFA Flow: OTT only
                .mfa(mfa -> mfa
                        .name("user")
                        .urlPrefix("/user")
                        .primaryAuthentication(auth -> auth
                                .formLogin(form -> form
                                        .defaultSuccessUrl("/user/dashboard")))
                        .ott(Customizer.withDefaults())
                        .order(70)
                ).session(Customizer.withDefaults())

                .build();
    }
}

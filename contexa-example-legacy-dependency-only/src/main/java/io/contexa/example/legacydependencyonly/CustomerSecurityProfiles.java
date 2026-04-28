package io.contexa.example.legacydependencyonly;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Map;

class CustomerSecurityProfiles {

    private CustomerSecurityProfiles() {
    }

    @Configuration(proxyBeanMethods = false)
    @Profile({"customer-basic-stateless", "customer-form-login", "customer-multi-chain", "customer-actuator-split"})
    static class CustomerUsers {

        @Bean
        UserDetailsService customerUserDetailsService() {
            return new InMemoryUserDetailsManager(User.withUsername("customer")
                    .password("{noop}secret")
                    .roles("USER")
                    .build());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("customer-basic-stateless")
    static class CustomerBasicStatelessSecurityConfiguration {

        @Bean
        SecurityFilterChain customerBasicStatelessChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("customer-form-login")
    static class CustomerFormLoginSecurityConfiguration {

        @Bean
        SecurityFilterChain customerFormLoginChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/login", "/public/**").permitAll()
                            .anyRequest().authenticated())
                    .formLogin(Customizer.withDefaults())
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("customer-multi-chain")
    static class CustomerMultiChainSecurityConfiguration {

        @Bean
        @Order(1)
        SecurityFilterChain customerApiChain(HttpSecurity http) throws Exception {
            return http
                    .securityMatcher("/api/**")
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }

        @Bean
        @Order(2)
        SecurityFilterChain customerPublicChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("customer-resource-server-jwt")
    static class CustomerResourceServerJwtSecurityConfiguration {

        @Bean
        SecurityFilterChain customerJwtResourceServerChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                    .build();
        }

        @Bean
        JwtDecoder customerJwtDecoder() {
            return token -> {
                if (!"valid-token".equals(token)) {
                    throw new JwtException("invalid token");
                }
                Instant now = Instant.now();
                return new Jwt(token, now, now.plusSeconds(300),
                        Map.of("alg", "none"),
                        Map.of("sub", "customer-user", "scope", "read"));
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("customer-oauth2-login")
    static class CustomerOAuth2LoginSecurityConfiguration {

        @Bean
        SecurityFilterChain customerOAuth2LoginChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/public/**").permitAll()
                            .anyRequest().authenticated())
                    .oauth2Login(Customizer.withDefaults())
                    .build();
        }

        @Bean
        ClientRegistrationRepository customerClientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration
                    .withRegistrationId("mock")
                    .clientId("customer-client")
                    .clientSecret("customer-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile")
                    .authorizationUri("https://idp.example.test/oauth2/authorize")
                    .tokenUri("https://idp.example.test/oauth2/token")
                    .userInfoUri("https://idp.example.test/oauth2/userinfo")
                    .userNameAttributeName("sub")
                    .clientName("Mock Customer IdP")
                    .build();
            return new InMemoryClientRegistrationRepository(registration);
        }

        @Bean
        OAuth2AuthorizedClientService customerAuthorizedClientService(
                ClientRegistrationRepository clientRegistrationRepository) {
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("customer-actuator-split")
    static class CustomerActuatorSplitSecurityConfiguration {

        @Bean
        SecurityFilterChain customerActuatorSplitChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(EndpointRequest.to("health")).permitAll()
                            .requestMatchers("/actuator/health").permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }
}

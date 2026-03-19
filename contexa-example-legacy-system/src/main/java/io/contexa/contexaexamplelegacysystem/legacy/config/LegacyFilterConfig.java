package io.contexa.contexaexamplelegacysystem.legacy.config;

import io.contexa.contexaexamplelegacysystem.legacy.filter.LegacyAuthFilter;
import io.contexa.contexaexamplelegacysystem.legacy.service.LegacyUserService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers legacy servlet filters. This is standard Spring Boot filter registration,
 * completely independent of Spring Security.
 */
@Configuration
public class LegacyFilterConfig {

    @Bean
    public FilterRegistrationBean<LegacyAuthFilter> legacyAuthFilter(LegacyUserService userService) {
        FilterRegistrationBean<LegacyAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LegacyAuthFilter(userService));
        registration.addUrlPatterns("/*");
        registration.setOrder(-200);
        registration.setName("legacyAuthFilter");
        return registration;
    }
}

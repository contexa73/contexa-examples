package io.contexa.contexaexamplelegacysystem.legacy.config;

import io.contexa.contexaexamplelegacysystem.legacy.interceptor.LegacyAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Legacy MVC configuration. Registers authorization interceptor.
 */
@Configuration
public class LegacyWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LegacyAuthorizationInterceptor())
                .addPathPatterns("/legacy/**", "/api/**");
    }
}

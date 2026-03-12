package io.contexa.example.quickstart;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Contexa setup.
 *
 * Just add @EnableAISecurity — the default PlatformConfig is created automatically
 * via IdentityDslRegistry. No custom SecurityConfig required for basic usage.
 */
@SpringBootApplication
@EnableAISecurity
public class QuickstartApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickstartApplication.class, args);
    }
}

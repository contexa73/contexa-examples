package io.contexa.example.protectable;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import io.contexa.contexacommon.security.bridge.SecurityMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Demonstrates @Protectable annotation patterns for method-level AI security.
 */
@SpringBootApplication
@EnableAISecurity(mode = SecurityMode.FULL)
@EnableJpaRepositories(basePackages = "io.contexa.example.protectable.domain")
public class ProtectableApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProtectableApplication.class, args);
    }
}

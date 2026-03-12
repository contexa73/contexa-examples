package io.contexa.example.protectable;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates @Protectable annotation patterns for method-level AI security.
 */
@SpringBootApplication
@EnableAISecurity
public class ProtectableApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProtectableApplication.class, args);
    }
}

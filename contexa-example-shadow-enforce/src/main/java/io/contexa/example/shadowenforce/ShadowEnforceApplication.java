package io.contexa.example.shadowenforce;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates Shadow Mode vs Enforce Mode transition.
 *
 * Run with Spring profiles to switch modes:
 *
 *   Shadow mode  (observe only, no blocking):
 *     ./gradlew :contexa-example-shadow-enforce:bootRun --args='--spring.profiles.active=shadow'
 *
 *   Enforce mode (AI decisions are enforced):
 *     ./gradlew :contexa-example-shadow-enforce:bootRun --args='--spring.profiles.active=enforce'
 *
 * Shadow mode is recommended for initial deployment:
 *   1. Deploy with shadow mode for 2-4 weeks
 *   2. HCAD baseline learns normal user behavior
 *   3. Monitor audit logs for false positives
 *   4. Switch to enforce mode once baseline is stable
 */
@SpringBootApplication
@EnableAISecurity
public class ShadowEnforceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShadowEnforceApplication.class, args);
    }
}

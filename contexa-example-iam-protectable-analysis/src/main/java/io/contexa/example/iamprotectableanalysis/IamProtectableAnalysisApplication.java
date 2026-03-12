package io.contexa.example.iamprotectableanalysis;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Protectable method security with real-time LLM analysis visualization.
 * Demonstrates 5 AnalysisRequirement levels and SSE-based analysis streaming.
 */
@SpringBootApplication
@EnableAISecurity
public class IamProtectableAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamProtectableAnalysisApplication.class, args);
    }
}

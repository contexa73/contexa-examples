package io.contexa.example.quickstart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple REST controller.
 *
 * All endpoints are automatically protected by Contexa's Zero Trust engine
 * through the default PlatformConfig created by @EnableAISecurity.
 */
@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from Contexa!",
                "status", "protected by Zero Trust"
        );
    }

    @GetMapping("/api/hello/{name}")
    public Map<String, String> helloName(@PathVariable String name) {
        return Map.of(
                "message", "Hello, " + name + "!",
                "status", "protected by Zero Trust"
        );
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}

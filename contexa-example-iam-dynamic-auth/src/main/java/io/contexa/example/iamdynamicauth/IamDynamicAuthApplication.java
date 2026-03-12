package io.contexa.example.iamdynamicauth;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * URL dynamic authorization example.
 * Demonstrates policy-based URL authorization with AI security expressions.
 */
@SpringBootApplication
@EnableAISecurity
public class IamDynamicAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamDynamicAuthApplication.class, args);
    }
}

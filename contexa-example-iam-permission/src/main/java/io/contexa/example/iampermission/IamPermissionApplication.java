package io.contexa.example.iampermission;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Permission evaluator example.
 * Demonstrates hasPermission() with custom DomainPermissionEvaluator.
 */
@SpringBootApplication
@EnableAISecurity
public class IamPermissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamPermissionApplication.class, args);
    }
}

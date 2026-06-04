package io.contexa.example.iampermission;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Permission evaluator example.
 * Demonstrates hasPermission() with custom DomainPermissionEvaluator.
 */
@SpringBootApplication
@EnableAISecurity
@EnableJpaRepositories(basePackages = "io.contexa.example.iampermission.repository")
public class IamPermissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamPermissionApplication.class, args);
    }
}

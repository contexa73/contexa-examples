package io.contexa.example.identityrest;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class IdentityRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityRestApplication.class, args);
    }
}

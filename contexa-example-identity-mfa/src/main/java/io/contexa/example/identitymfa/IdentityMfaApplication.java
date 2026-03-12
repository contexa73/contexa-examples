package io.contexa.example.identitymfa;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class IdentityMfaApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityMfaApplication.class, args);
    }
}

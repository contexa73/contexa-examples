package io.contexa.example.identityasep;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class IdentityAsepApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityAsepApplication.class, args);
    }
}

package io.contexa.example.identityott;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class IdentityOttApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityOttApplication.class, args);
    }
}

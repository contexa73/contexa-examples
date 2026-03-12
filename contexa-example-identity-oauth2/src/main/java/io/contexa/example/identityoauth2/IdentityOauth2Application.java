package io.contexa.example.identityoauth2;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class IdentityOauth2Application {
    public static void main(String[] args) {
        SpringApplication.run(IdentityOauth2Application.class, args);
    }
}

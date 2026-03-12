package io.contexa.example.aillm;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class AiLlmApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiLlmApplication.class, args);
    }
}

package io.contexa.example.ailab;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class AiLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiLabApplication.class, args);
    }
}

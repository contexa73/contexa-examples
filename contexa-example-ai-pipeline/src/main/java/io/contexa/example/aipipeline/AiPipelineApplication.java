package io.contexa.example.aipipeline;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class AiPipelineApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiPipelineApplication.class, args);
    }
}

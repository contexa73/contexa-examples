package io.contexa.contexaexampleidentitymfamulti;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAISecurity
public class ContexaExampleIdentityMfaMultiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContexaExampleIdentityMfaMultiApplication.class, args);
    }
}

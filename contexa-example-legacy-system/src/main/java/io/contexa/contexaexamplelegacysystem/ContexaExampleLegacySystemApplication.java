package io.contexa.contexaexamplelegacysystem;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import io.contexa.contexacommon.security.bridge.SecurityMode;
import io.contexa.contexacommon.security.bridge.SessionAuthBridge;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Legacy system with Contexa AI Zero Trust protection.
 * <p>
 * SANDBOX mode: Legacy authentication is bridged via SessionAuthBridge.
 * Only @Protectable resources are protected by Contexa.
 * Legacy security (LegacyAuthFilter, LegacyAuthorizationInterceptor) is untouched.
 */
@EnableAISecurity(
        mode = SecurityMode.SANDBOX,
        authBridge = SessionAuthBridge.class,
        sessionUserAttribute = "legacyUser"
)
@SpringBootApplication
public class ContexaExampleLegacySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContexaExampleLegacySystemApplication.class, args);
    }
}

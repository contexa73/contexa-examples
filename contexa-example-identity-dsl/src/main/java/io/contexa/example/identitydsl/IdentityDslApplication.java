package io.contexa.example.identitydsl;

import io.contexa.contexacommon.annotation.EnableAISecurity;
import io.contexa.contexacommon.security.bridge.SecurityMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates custom PlatformSecurityConfig using the Identity DSL.
 *
 * Unlike the quickstart example, this module defines its own PlatformConfig bean
 * using IdentityDslRegistry to customize authentication flows, authorization rules,
 * and session management.
 */
@SpringBootApplication
@EnableAISecurity(mode = SecurityMode.FULL)
public class IdentityDslApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityDslApplication.class, args);
    }
}

package io.contexa.example.legacydependencyonly;

import io.contexa.contexaidentity.security.core.config.PlatformConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class LegacyDependencyOnlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyDependencyOnlyApplication.class, args);
    }

    @RestController
    static class PingController {

        private final ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider;
        private final ObjectProvider<PlatformConfig> platformConfigProvider;

        PingController(
                ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider,
                ObjectProvider<PlatformConfig> platformConfigProvider) {
            this.redisConnectionFactoryProvider = redisConnectionFactoryProvider;
            this.platformConfigProvider = platformConfigProvider;
        }

        @GetMapping("/ping")
        String ping() {
            return "ok";
        }

        @GetMapping({"/public/ping", "/api/ping", "/oauth2-protected"})
        String namedPing() {
            return "ok";
        }

        @GetMapping("/infra/redis-factory")
        String redisFactory() {
            return redisConnectionFactoryProvider.getIfAvailable() != null ? "redis=true" : "redis=false";
        }

        @GetMapping("/contexa/platform-config")
        String contexaPlatformConfig() {
            return platformConfigProvider.getIfAvailable() != null ? "platform=true" : "platform=false";
        }
    }
}

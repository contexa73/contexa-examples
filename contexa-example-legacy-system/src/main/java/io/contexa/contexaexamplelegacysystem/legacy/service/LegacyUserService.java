package io.contexa.contexaexamplelegacysystem.legacy.service;

import io.contexa.contexaexamplelegacysystem.legacy.model.LegacyUser;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legacy user management service.
 * Simulates a complex enterprise user store with multiple auth methods.
 */
@Service
public class LegacyUserService {

    private final Map<String, LegacyUser> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, LegacyUser> usersByToken = new ConcurrentHashMap<>();
    private final Map<String, LegacyUser> usersByCookie = new ConcurrentHashMap<>();

    public LegacyUserService() {
        // Simulate enterprise users with different roles and departments
        LegacyUser admin = new LegacyUser(
                "USR001", "admin", "admin123", "System Administrator",
                Set.of("ADMIN", "MANAGER"), "IT", 10,
                "tok-admin-a1b2c3d4e5f6");

        LegacyUser manager = new LegacyUser(
                "USR002", "kim_manager", "manager123", "Kim Jihoon",
                Set.of("MANAGER"), "Finance", 7,
                "tok-manager-f6e5d4c3b2a1");

        LegacyUser developer = new LegacyUser(
                "USR003", "park_dev", "dev123", "Park Minjun",
                Set.of("DEVELOPER"), "Engineering", 5,
                "tok-dev-1a2b3c4d5e6f");

        LegacyUser analyst = new LegacyUser(
                "USR004", "lee_analyst", "analyst123", "Lee Soyeon",
                Set.of("ANALYST"), "Data", 3,
                null);

        for (LegacyUser user : new LegacyUser[]{admin, manager, developer, analyst}) {
            usersByUsername.put(user.username(), user);
            if (user.apiToken() != null) {
                usersByToken.put(user.apiToken(), user);
            }
        }
    }

    public LegacyUser authenticate(String username, String password) {
        LegacyUser user = usersByUsername.get(username);
        if (user != null && user.password().equals(password)) {
            return user;
        }
        return null;
    }

    public LegacyUser authenticateByToken(String token) {
        return usersByToken.get(token);
    }

    public LegacyUser authenticateByRememberMe(String cookieValue) {
        return usersByCookie.get(cookieValue);
    }

    public void registerRememberMe(String cookieValue, LegacyUser user) {
        usersByCookie.put(cookieValue, user);
    }
}

package io.contexa.contexaexamplelegacysystem.legacy.model;

import java.util.Set;

/**
 * Legacy user entity. In real systems this would come from a database.
 */
public record LegacyUser(
        String userId,
        String username,
        String password,
        String displayName,
        Set<String> roles,
        String department,
        int accessLevel,
        String apiToken
) {}

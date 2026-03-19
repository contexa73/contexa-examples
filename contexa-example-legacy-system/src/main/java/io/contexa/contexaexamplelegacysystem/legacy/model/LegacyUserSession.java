package io.contexa.contexaexamplelegacysystem.legacy.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Legacy session object stored in HttpSession.
 * This represents a typical enterprise legacy system's user session.
 */
public class LegacyUserSession implements Serializable {

    private String userId;
    private String username;
    private String displayName;
    private Set<String> roles;
    private LocalDateTime loginTime;
    private String loginIp;
    private String authMethod;   // FORM, TOKEN, REMEMBER_ME
    private String department;
    private int accessLevel;
    private Map<String, Object> customAttributes;

    public LegacyUserSession() {}

    public LegacyUserSession(String userId, String username, String displayName,
                              Set<String> roles, String loginIp, String authMethod,
                              String department, int accessLevel) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.roles = roles;
        this.loginTime = LocalDateTime.now();
        this.loginIp = loginIp;
        this.authMethod = authMethod;
        this.department = department;
        this.accessLevel = accessLevel;
        this.customAttributes = Map.of();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    public String getAuthMethod() { return authMethod; }
    public void setAuthMethod(String authMethod) { this.authMethod = authMethod; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getAccessLevel() { return accessLevel; }
    public void setAccessLevel(int accessLevel) { this.accessLevel = accessLevel; }
    public Map<String, Object> getCustomAttributes() { return customAttributes; }
    public void setCustomAttributes(Map<String, Object> customAttributes) { this.customAttributes = customAttributes; }

    @Override
    public String toString() {
        return "LegacyUserSession{userId='" + userId + "', roles=" + roles +
                ", authMethod='" + authMethod + "', dept='" + department + "'}";
    }
}

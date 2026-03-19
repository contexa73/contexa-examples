package io.contexa.contexaexamplelegacysystem.legacy.controller;

import io.contexa.contexaexamplelegacysystem.legacy.annotation.LegacyRole;
import io.contexa.contexaexamplelegacysystem.legacy.filter.LegacyAuthFilter;
import io.contexa.contexaexamplelegacysystem.legacy.model.LegacyUserSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Legacy dashboard. Protected by LegacyAuthFilter (session check).
 * Role-based access via @LegacyRole and LegacyAuthorizationInterceptor.
 * No Spring Security, no Contexa involvement.
 */
@Controller
public class LegacyDashboardController {

    @GetMapping("/legacy/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        LegacyUserSession user = getUserSession(request);
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "legacy-dashboard";
    }

    @LegacyRole({"ADMIN"})
    @GetMapping("/legacy/admin")
    public String adminPage(HttpServletRequest request, Model model) {
        LegacyUserSession user = getUserSession(request);
        model.addAttribute("user", user);
        return "legacy-admin";
    }

    private LegacyUserSession getUserSession(HttpServletRequest request) {
        if (request.getSession(false) == null) return null;
        return (LegacyUserSession) request.getSession().getAttribute(LegacyAuthFilter.SESSION_USER_KEY);
    }
}

package io.contexa.contexaexamplelegacysystem.legacy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Legacy login/logout pages. No Spring Security involved.
 */
@Controller
public class LegacyAuthController {

    @GetMapping("/")
    public String home() {
        return "redirect:/legacy/login";
    }

    @GetMapping("/legacy/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if ("invalid".equals(error)) {
            model.addAttribute("error", "Invalid username or password");
        }
        if ("true".equals(logout)) {
            model.addAttribute("message", "Logged out successfully");
        }
        return "legacy-login";
    }
}

package io.contexa.example.shadowenforce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the Shadow/Enforce example.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/shadow-enforce")
    public String shadowEnforceTestPage() {
        return "test/shadow-enforce-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

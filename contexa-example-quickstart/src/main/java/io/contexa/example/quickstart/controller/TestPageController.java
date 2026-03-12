package io.contexa.example.quickstart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the quickstart example.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/quickstart")
    public String quickstartTestPage() {
        return "test/quickstart-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

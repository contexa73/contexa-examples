package io.contexa.example.protectable.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the @Protectable example.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/protectable")
    public String protectableTestPage() {
        return "test/protectable-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

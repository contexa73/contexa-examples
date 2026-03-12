package io.contexa.example.identityasep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the ASEP example.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/asep")
    public String asepTestPage() {
        return "test/asep-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

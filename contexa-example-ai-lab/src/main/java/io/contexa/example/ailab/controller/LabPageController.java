package io.contexa.example.ailab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the Lab UI.
 */
@Controller
public class LabPageController {

    @GetMapping("/lab/test")
    public String labTestPage() {
        return "test/lab-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

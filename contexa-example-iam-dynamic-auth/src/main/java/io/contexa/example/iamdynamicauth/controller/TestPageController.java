package io.contexa.example.iamdynamicauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the IAM Dynamic Auth example.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/dynamic-auth")
    public String dynamicAuthTestPage() {
        return "test/dynamic-auth-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

package io.contexa.example.iampermission.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the IAM Permission example.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/permission")
    public String permissionTestPage() {
        return "test/permission-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

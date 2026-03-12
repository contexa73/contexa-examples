package io.contexa.example.identityoauth2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Token test page controller.
 * Serves the token-test.html template for interactive OAuth2 token testing.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/token")
    public String tokenTestPage() {
        return "test/token-test";
    }
}

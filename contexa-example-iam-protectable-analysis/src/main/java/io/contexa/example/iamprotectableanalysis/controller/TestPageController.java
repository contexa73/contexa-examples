package io.contexa.example.iamprotectableanalysis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Security test page controller.
 * Serves the security-test.html template for interactive @Protectable testing.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/security")
    public String securityTestPage() {
        return "test/security-test";
    }
}

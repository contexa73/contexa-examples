package io.contexa.example.identityrest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the REST login test page.
 */
@Controller
public class TestPageController {

    @GetMapping("/test/rest")
    public String restTestPage() {
        return "test/rest-test";
    }
}

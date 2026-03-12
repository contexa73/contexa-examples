package io.contexa.example.aillm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the LLM UI.
 */
@Controller
public class LlmPageController {

    @GetMapping("/llm/test")
    public String llmTestPage() {
        return "test/llm-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

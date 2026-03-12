package io.contexa.example.aipipeline.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf test pages for the Pipeline UI.
 */
@Controller
public class PipelinePageController {

    @GetMapping("/pipeline/test")
    public String pipelineTestPage() {
        return "test/pipeline-test";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}

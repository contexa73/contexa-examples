package io.contexa.example.identityott.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OttPageController {

    @GetMapping("/ott/request")
    public String requestPage() {
        return "ott-request";
    }

    @GetMapping("/ott/verify")
    public String verifyPage() {
        return "ott-verify";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}

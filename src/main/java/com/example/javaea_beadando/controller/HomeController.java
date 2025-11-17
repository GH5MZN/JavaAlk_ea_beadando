package com.example.javaea_beadando.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "welcome";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "index";
    }
}


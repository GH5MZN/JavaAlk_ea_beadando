package com.example.java_beadando_ea_xndivegh5mzn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Az alkalmazás fut! Ha ezt látod, akkor a Spring Boot működik.";
    }

    @GetMapping("/api/status")
    public String status() {
        return "{\"status\":\"OK\",\"message\":\"Application is running\"}";
    }
}

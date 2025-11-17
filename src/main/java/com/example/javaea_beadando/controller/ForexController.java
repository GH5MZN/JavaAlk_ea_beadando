package com.example.javaea_beadando.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForexController {

    @GetMapping("/forex-account")
    public String forexAccount(Model model) {
        model.addAttribute("title", "FOREX Account");
        return "forex/account";
    }

    @GetMapping("/forex-aktar")
    public String forexAktar(Model model) {
        model.addAttribute("title", "FOREX AktÁr");
        return "forex/aktar";
    }

    @GetMapping("/forex-histar")
    public String forexHistar(Model model) {
        model.addAttribute("title", "FOREX HistÁr");
        return "forex/histar";
    }

    @GetMapping("/forex-nyit")
    public String forexNyit(Model model) {
        model.addAttribute("title", "FOREX Nyit");
        return "forex/nyit";
    }

    @GetMapping("/forex-poz")
    public String forexPoz(Model model) {
        model.addAttribute("title", "FOREX Poz");
        return "forex/poz";
    }

    @GetMapping("/forex-zar")
    public String forexZar(Model model) {
        model.addAttribute("title", "FOREX Zár");
        return "forex/zar";
    }
}


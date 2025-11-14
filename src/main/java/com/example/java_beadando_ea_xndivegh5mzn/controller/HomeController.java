package com.example.java_beadando_ea_xndivegh5mzn.controller;

import com.example.java_beadando_ea_xndivegh5mzn.dto.ExchangeRate;
import com.example.java_beadando_ea_xndivegh5mzn.service.MNBSoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private MNBSoapService mnbSoapService;

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/soap")
    public String soap(Model model) {
        return "soap";
    }

    @PostMapping("/soap/query")
    public String soapQuery(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        try {
            // Get exchange rates from MNB SOAP service
            List<ExchangeRate> rates = mnbSoapService.getExchangeRates(currency, startDate, endDate);

            model.addAttribute("currency", currency);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("exchangeRates", rates);

            if (rates.isEmpty()) {
                model.addAttribute("message", "Nincsenek adatok a megadott időszakra és devizára.");
            } else {
                model.addAttribute("message", "Sikeresen lekérdezve " + rates.size() + " árfolyam adat.");

                // Prepare chart data
                List<String> dates = rates.stream()
                    .map(rate -> rate.getDate().toString())
                    .collect(Collectors.toList());
                List<Double> rateValues = rates.stream()
                    .map(rate -> rate.getRate().doubleValue())
                    .collect(Collectors.toList());

                model.addAttribute("chartDates", dates);
                model.addAttribute("chartRates", rateValues);
                model.addAttribute("hasChartData", true);
            }

        } catch (Exception e) {
            model.addAttribute("currency", currency);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("message", "Hiba történt a lekérdezés során: " + e.getMessage());
        }

        return "soap";
    }

    @GetMapping("/forex-account")
    public String forexAccount(Model model) {
        // TODO: Implement account info display
        model.addAttribute("message", "Számla információk implementálás alatt...");
        return "forex-account";
    }

    @GetMapping("/forex-aktar")
    public String forexAktar(Model model) {
        // TODO: Implement current price display
        return "forex-aktar";
    }

    @GetMapping("/forex-histar")
    public String forexHistar(Model model) {
        // TODO: Implement historical prices
        return "forex-histar";
    }

    @GetMapping("/forex-nyit")
    public String forexNyit(Model model) {
        // TODO: Implement position opening
        return "forex-nyit";
    }

    @GetMapping("/forex-poz")
    public String forexPoz(Model model) {
        // TODO: Implement positions display
        return "forex-poz";
    }

    @GetMapping("/forex-zar")
    public String forexZar(Model model) {
        // TODO: Implement position closing
        return "forex-zar";
    }
}

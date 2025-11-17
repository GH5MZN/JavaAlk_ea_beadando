package com.example.javaea_beadando.controller;

import com.example.javaea_beadando.forex.Config;
import com.example.javaea_beadando.forex.MessageActPrice;
import com.example.javaea_beadando.forex.MessageHistPrice;
import com.example.javaea_beadando.forex.TradeApplication;
import com.oanda.v20.Context;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;


import java.util.ArrayList;
import java.util.List;

import static com.oanda.v20.instrument.CandlestickGranularity.*;

@Controller
public class ForexController {

    private final Context ctx = Config.getContext();

    @GetMapping("/forex-account")
    public String forexAccount(Model model) {
        // Példa TradeApplication adat létrehozása
        TradeApplication tradeApp = new TradeApplication();
        tradeApp.setAccountId("101-004-1234567-001");
        tradeApp.setAccountName("Primary Trading Account");
        tradeApp.setCurrency("USD");
        tradeApp.setBalance(10000.00);
        tradeApp.setUnrealizedPL(250.50);
        tradeApp.setMarginUsed(2500.00);
        tradeApp.setMarginAvailable(7500.00);
        tradeApp.setOpenTradeCount(5);
        tradeApp.setOpenPositionCount(3);

        model.addAttribute("title", "FOREX Account");
        model.addAttribute("tradeApp", tradeApp);
        return "forex/account";
    }

    @GetMapping("/forex-aktar")
    public String forexAktar(Model model) {
        model.addAttribute("title", "FOREX AktÁr");
        model.addAttribute("par", new MessageActPrice());
        return "forex/aktar";
    }

    @PostMapping("/forex-aktar")
    public String forexAktarResult(@ModelAttribute MessageActPrice messageActPrice, Model model) {
        StringBuilder strOut = new StringBuilder();

        List<String> instruments = new ArrayList<>();
        instruments.add(messageActPrice.getInstrument());

        try {
            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instruments);
            PricingGetResponse resp = ctx.pricing.get(request);

            if (resp.getPrices().isEmpty()) {
                strOut.append("<p class='text-warning'>Nem található árfolyam adat a következő instrumenthez: ")
                      .append(messageActPrice.getInstrument())
                      .append("</p>");
            } else {
                strOut.append("<table class='table table-bordered table-striped'>");
                strOut.append("<thead class='thead-dark'>");
                strOut.append("<tr><th>Instrument</th><th>Bid</th><th>Ask</th><th>Spread</th><th>Time</th></tr>");
                strOut.append("</thead><tbody>");

                for (ClientPrice price : resp.getPrices()) {
                    strOut.append("<tr>");
                    strOut.append("<td><strong>").append(price.getInstrument()).append("</strong></td>");
                    strOut.append("<td class='text-danger'>").append(price.getBids().get(0).getPrice()).append("</td>");
                    strOut.append("<td class='text-success'>").append(price.getAsks().get(0).getPrice()).append("</td>");

                    // Spread számítása
                    double bid = Double.parseDouble(price.getBids().get(0).getPrice().toString());
                    double ask = Double.parseDouble(price.getAsks().get(0).getPrice().toString());
                    double spread = ask - bid;
                    strOut.append("<td>").append(String.format("%.5f", spread)).append("</td>");

                    strOut.append("<td>").append(price.getTime()).append("</td>");
                    strOut.append("</tr>");
                }
                strOut.append("</tbody></table>");
            }
        } catch (Exception e) {
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5><i class='fas fa-exclamation-circle'></i> Hiba történt</h5>");
            strOut.append("<p><strong>Hibaüzenet:</strong> ").append(e.getMessage()).append("</p>");

            if (e.getMessage() != null && e.getMessage().contains("module java.base does not")) {
                strOut.append("<hr>");
                strOut.append("<p><strong>Java Modularitási Hiba:</strong></p>");
                strOut.append("<p>Ez a hiba a Java 9+ modul rendszer miatt jelentkezik. Megoldások:</p>");
                strOut.append("<ol>");
                strOut.append("<li>Futtasd az alkalmazást a következő JVM paraméterekkel:<br>");
                strOut.append("<code>--add-opens java.base/java.lang=ALL-UNNAMED</code></li>");
                strOut.append("<li>Vagy használj Java 8-at</li>");
                strOut.append("<li>Vagy frissítsd a Gson library-t a legújabb verzióra</li>");
                strOut.append("</ol>");
            } else if (e.getMessage() != null && e.getMessage().contains("Authorization")) {
                strOut.append("<p>Ellenőrizd, hogy az API token helyes-e a Config.java fájlban!</p>");
            }

            strOut.append("</div>");
        }

        model.addAttribute("title", "FOREX AktÁr - Eredmény");
        model.addAttribute("instr", messageActPrice.getInstrument());
        model.addAttribute("price", strOut.toString());
        return "forex/aktar_result";
    }

    @GetMapping("/forex-histar")
    public String forexHistar(Model model) {
        model.addAttribute("title", "FOREX HistÁr");
        model.addAttribute("par", new MessageHistPrice());
        return "forex/histar";
    }

    @PostMapping("/forex-histar")
    public String forexHistarResult(@ModelAttribute MessageHistPrice messageHistPrice, Model model) {
        StringBuilder strOut = new StringBuilder();

        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new
                    InstrumentName(messageHistPrice.getInstrument()));

            switch (messageHistPrice.getGranularity()) {
                case "M1": request.setGranularity(M1); break;
                case "H1": request.setGranularity(H1); break;
                case "D": request.setGranularity(D); break;
                case "W": request.setGranularity(W); break;
                case "M": request.setGranularity(M); break;
            }

            request.setCount(Long.valueOf(10)); // utolsó 10 árat kérjünk
            InstrumentCandlesResponse resp = ctx.instrument.candles(request);

            strOut.append("<table class='table table-bordered table-striped'>");
            strOut.append("<thead class='thead-dark'>");
            strOut.append("<tr><th>#</th><th>Time</th><th>Open</th><th>High</th><th>Low</th><th>Close</th></tr>");
            strOut.append("</thead><tbody>");

            int count = 1;
            for (Candlestick candle : resp.getCandles()) {
                strOut.append("<tr>");
                strOut.append("<td>").append(count++).append("</td>");
                strOut.append("<td>").append(candle.getTime()).append("</td>");
                strOut.append("<td>").append(candle.getMid().getO()).append("</td>");
                strOut.append("<td class='text-success'>").append(candle.getMid().getH()).append("</td>");
                strOut.append("<td class='text-danger'>").append(candle.getMid().getL()).append("</td>");
                strOut.append("<td><strong>").append(candle.getMid().getC()).append("</strong></td>");
                strOut.append("</tr>");
            }
            strOut.append("</tbody></table>");

        } catch (Exception e) {
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5><i class='fas fa-exclamation-circle'></i> Hiba történt</h5>");
            strOut.append("<p><strong>Hibaüzenet:</strong> ").append(e.getMessage()).append("</p>");
            strOut.append("</div>");
        }

        model.addAttribute("title", "FOREX HistÁr - Eredmény");
        model.addAttribute("instr", messageHistPrice.getInstrument());
        model.addAttribute("granularity", messageHistPrice.getGranularity());
        model.addAttribute("price", strOut.toString());
        return "forex/histar_result";
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


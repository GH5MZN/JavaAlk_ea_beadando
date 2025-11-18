package com.example.javaea_beadando.controller;

import com.example.javaea_beadando.forex.Config;
import com.example.javaea_beadando.forex.MessageActPrice;
import com.example.javaea_beadando.forex.MessageHistPrice;
import com.example.javaea_beadando.forex.TradeApplication;
import com.oanda.v20.Context;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeListResponse;
import com.oanda.v20.trade.TradeSpecifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;
import org.springframework.web.bind.annotation.RequestParam;


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

    @PostMapping("/forex-nyit")
    public String forexNyitResult(
            @ModelAttribute("instrument") String instrument,
            @ModelAttribute("units") int units,
            Model model
    ) {
        StringBuilder strOut = new StringBuilder();

        try {
            // Market order létrehozása OANDA-n
            OrderCreateRequest orderReq = new OrderCreateRequest(Config.ACCOUNTID);

            MarketOrderRequest marketOrder = new MarketOrderRequest();
            marketOrder.setInstrument(instrument);
            marketOrder.setUnits(String.valueOf(units)); // Long = pozitív, Short = negatív

            orderReq.setOrder(marketOrder);

            // Order elküldése
            OrderCreateResponse resp = ctx.order.create(orderReq);

            strOut.append("<div class='alert alert-success'>");
            strOut.append("<h5>Pozíció megnyitva!</h5>");
            strOut.append("<p><strong>Instrument:</strong> ").append(instrument).append("</p>");
            strOut.append("<p><strong>Mennyiség:</strong> ").append(units).append("</p>");
            strOut.append("<p><strong>Order ID:</strong> ").append(resp.getOrderCreateTransaction().getId()).append("</p>");
            strOut.append("</div>");

        } catch (Exception e) {
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5>Hiba történt a pozíció nyitásakor!</h5>");
            strOut.append("<p><strong>Hibaüzenet:</strong> ").append(e.getMessage()).append("</p>");

            if (e.getMessage() != null && e.getMessage().contains("Authorization")) {
                strOut.append("<p>Ellenőrizd, hogy az API token helyesen van-e megadva a Config.java-ban!</p>");
            }

            strOut.append("</div>");
        }

        model.addAttribute("title", "FOREX Nyit - Eredmény");
        model.addAttribute("result", strOut.toString());

        return "forex/nyit_result";
    }
    @GetMapping("/forex-nyit")
    public String forexNyitPage(Model model) {
        model.addAttribute("title", "FOREX Nyitás");
        return "forex/nyit"; // nyit.html
    }

    @GetMapping("/forex-poz")
    public String forexPozok(Model model) {

        try {

            TradeListResponse response = ctx.trade.list(Config.ACCOUNTID);
            List<Trade> trades = response.getTrades();

            model.addAttribute("title", "Nyitott pozíciók");
            model.addAttribute("trades", trades);

        } catch (Exception e) {
            model.addAttribute("title", "Hiba");
            model.addAttribute("error", "Nem sikerült lekérni a nyitott pozíciókat: " + e.getMessage());
        }

        return "forex/poz"; // poz.html
    }
    @GetMapping("/forex-zar")
    public String forexZarPage(Model model) {
        model.addAttribute("title", "Pozíció zárása");
        return "forex/zar"; // zar.html
    }
    /*@PostMapping("/forex-zar")
    public String forexZarSubmit(Model model) {

        try {
            // Trade lezárása
            //var response = ctx.trade.close(Config.ACCOUNTID, tradeId);

            model.addAttribute("title", "Pozíció zárása");
            model.addAttribute("msg", "Sikeresen lezártad a(z) " + Config.ACCOUNTID + " számú pozíciót.");

        } catch (Exception e) {

            model.addAttribute("title", "Hiba");
            model.addAttribute("error",
                    "Nem sikerült lezárni a pozíciót (TradeID: " + Config.ACCOUNTID + "). Hiba: " + e.getMessage());
        }

        return "forex/zar"; // ugyanarra az oldalra tér vissza
    }*/
    @PostMapping("/forex-zar")
    public String Zaras(TradeListResponse TLR, Model model)
    {
        String tradeId= TLR.getTrades()+"";
        String strOut="Closed tradeId= "+tradeId;
        try
        {
            ctx.trade.close(new TradeCloseRequest(Config.ACCOUNTID, new TradeSpecifier(tradeId)));
            model.addAttribute("title", "Pozíció zárása");
            model.addAttribute("msg", "Sikeresen lezártad a(z) " + Config.ACCOUNTID + " számú pozíciót.");
        } catch (Exception e)
        {
            model.addAttribute("title", "Hiba");
            model.addAttribute("error",
                    "Nem sikerült lezárni a pozíciót (TradeID: " + Config.ACCOUNTID + "). Hiba: " + e.getMessage());
        }
        model.addAttribute("tradeId", strOut); return "result_close_position"; }



}


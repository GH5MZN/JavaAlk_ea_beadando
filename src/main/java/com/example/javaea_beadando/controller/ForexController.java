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
import com.oanda.v20.trade.TradeClose404RequestException;
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
        try {
            // Valódi account adatok lekérése az OANDA API-ból
            com.oanda.v20.account.AccountGetResponse accountResp = ctx.account.get(Config.ACCOUNTID);
            com.oanda.v20.account.Account account = accountResp.getAccount();

            // TradeApplication objektum feltöltése valódi adatokkal
            TradeApplication tradeApp = new TradeApplication();
            tradeApp.setAccountId(account.getId().toString());
            tradeApp.setAccountName("OANDA Trading Account");
            tradeApp.setCurrency(account.getCurrency().toString());
            tradeApp.setBalance(Double.parseDouble(account.getBalance().toString()));
            tradeApp.setUnrealizedPL(Double.parseDouble(account.getUnrealizedPL().toString()));
            tradeApp.setMarginUsed(Double.parseDouble(account.getMarginUsed().toString()));
            tradeApp.setMarginAvailable(Double.parseDouble(account.getMarginAvailable().toString()));
            tradeApp.setOpenTradeCount(account.getOpenTradeCount().intValue());
            tradeApp.setOpenPositionCount(account.getOpenPositionCount().intValue());

            model.addAttribute("title", "FOREX Account");
            model.addAttribute("tradeApp", tradeApp);

        } catch (Exception e) {
            System.out.println("ERROR: Account adatok lekérése sikertelen: " + e.getMessage());
            e.printStackTrace();

            // Fallback - statikus adatok hiba esetén
            TradeApplication tradeApp = new TradeApplication();
            tradeApp.setAccountId(Config.ACCOUNTID.toString());
            tradeApp.setAccountName("OANDA Trading Account (Hiba)");
            tradeApp.setCurrency("USD");
            tradeApp.setBalance(0.00);
            tradeApp.setUnrealizedPL(0.00);
            tradeApp.setMarginUsed(0.00);
            tradeApp.setMarginAvailable(0.00);
            tradeApp.setOpenTradeCount(0);
            tradeApp.setOpenPositionCount(0);

            model.addAttribute("title", "FOREX Account");
            model.addAttribute("tradeApp", tradeApp);
            model.addAttribute("error", "Hiba az account adatok lekérésekor: " + e.getMessage());
        }

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
        List<String> labels = new ArrayList<>();
        List<Double> closeData = new ArrayList<>();
        List<Double> highData = new ArrayList<>();
        List<Double> lowData = new ArrayList<>();

        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(
                    new InstrumentName(messageHistPrice.getInstrument()));
            request.setGranularity(valueOf(messageHistPrice.getGranularity()));
            request.setCount(10L);

            InstrumentCandlesResponse resp = ctx.instrument.candles(request);
            List<Candlestick> candles = resp.getCandles();

            if (candles.isEmpty()) {
                strOut.append("<p class='text-warning'>Nincs elérhető historikus adat.</p>");
            } else {
                strOut.append("<table class='table table-bordered table-striped'>");
                strOut.append("<thead class='thead-dark'>");
                strOut.append("<tr><th>Időpont</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Volume</th></tr>");
                strOut.append("</thead><tbody>");

                for (Candlestick candle : candles) {
                    labels.add(candle.getTime().toString());
                    closeData.add(Double.parseDouble(candle.getMid().getC().toString()));
                    highData.add(Double.parseDouble(candle.getMid().getH().toString()));
                    lowData.add(Double.parseDouble(candle.getMid().getL().toString()));

                    strOut.append("<tr>");
                    strOut.append("<td>").append(candle.getTime()).append("</td>");
                    strOut.append("<td>").append(candle.getMid().getO()).append("</td>");
                    strOut.append("<td class='text-success'>").append(candle.getMid().getH()).append("</td>");
                    strOut.append("<td class='text-danger'>").append(candle.getMid().getL()).append("</td>");
                    strOut.append("<td><strong>").append(candle.getMid().getC()).append("</strong></td>");
                    strOut.append("<td>").append(candle.getVolume()).append("</td>");
                    strOut.append("</tr>");
                }
                strOut.append("</tbody></table>");
            }

        } catch (Exception e) {
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5><i class='fas fa-exclamation-circle'></i> Hiba történt</h5>");
            strOut.append("<p><strong>Hibaüzenet:</strong> ").append(e.getMessage()).append("</p>");

            if (e.getMessage() != null && e.getMessage().contains("Authorization")) {
                strOut.append("<p>Ellenőrizd, hogy az API token helyes-e a Config.java fájlban!</p>");
            }

            strOut.append("</div>");
        }

        model.addAttribute("title", "FOREX HistÁr - Eredmény");
        model.addAttribute("instrument", messageHistPrice.getInstrument());
        model.addAttribute("granularity", messageHistPrice.getGranularity());
        model.addAttribute("data", strOut.toString());
        model.addAttribute("labels", labels);
        model.addAttribute("closeData", closeData);
        model.addAttribute("highData", highData);
        model.addAttribute("lowData", lowData);

        return "forex/histar_result";
    }

    @PostMapping("/forex-nyit")
    public String forexNyitResult(
            @RequestParam("instrument") String instrument,
            @RequestParam("units") int units,
            Model model
    ) {
        StringBuilder strOut = new StringBuilder();

        try {

            OrderCreateRequest orderReq = new OrderCreateRequest(Config.ACCOUNTID);

            MarketOrderRequest marketOrder = new MarketOrderRequest();
            marketOrder.setInstrument(instrument);
            marketOrder.setUnits(String.valueOf(units)); // Long = pozitív, Short = negatív

            orderReq.setOrder(marketOrder);

            OrderCreateResponse resp = ctx.order.create(orderReq);

            strOut.append("<div class='alert alert-success'>");
            strOut.append("<h5>Pozíció megnyitva!</h5>");
            strOut.append("<p><strong>Instrument:</strong> ").append(instrument).append("</p>");
            strOut.append("<p><strong>Mennyiség:</strong> ").append(units).append("</p>");

            if (resp.getOrderFillTransaction() != null && resp.getOrderFillTransaction().getTradeOpened() != null) {
                strOut.append("<p><strong>Trade ID:</strong> ").append(resp.getOrderFillTransaction().getTradeOpened().getTradeID()).append("</p>");
                strOut.append("<p class='text-muted'><small>Ez a Trade ID-t használd a pozíció zárásakor!</small></p>");
            } else {
                strOut.append("<p><strong>Order ID:</strong> ").append(resp.getOrderCreateTransaction().getId()).append("</p>");
            }

            strOut.append("</div>");

            // Sikeres nyitás után redirect az account oldalra
            return "redirect:/forex-account";

        } catch (Exception e) {
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5>Hiba történt a pozíció nyitásakor!</h5>");
            strOut.append("<p><strong>Hibaüzenet:</strong> ").append(e.getMessage()).append("</p>");

            if (e.getMessage() != null && e.getMessage().contains("Authorization")) {
                strOut.append("<p>Ellenőrizd, hogy az API token helyesen van-e megadva a Config.java-ban!</p>");
            }

            strOut.append("</div>");

            model.addAttribute("title", "FOREX Nyit - Eredmény");
            model.addAttribute("result", strOut.toString());
            return "forex/nyit_result";
        }
    }
    @GetMapping("/forex-nyit")
    public String forexNyitPage(Model model) {
        model.addAttribute("title", "FOREX Nyitás");
        return "forex/nyit"; // nyit.html
    }

    @GetMapping("/forex-poz")
    public String forexPozok(Model model) {
        try {
            System.out.println("DEBUG: /forex-poz hívva");

            TradeListResponse response = ctx.trade.list(Config.ACCOUNTID);

            if (response == null) {
                System.out.println("DEBUG: response null!");
                model.addAttribute("title", "Nyitott pozíciók");
                model.addAttribute("trades", new ArrayList<>());
                return "forex/poz";
            }

            List<Trade> trades = response.getTrades();

            if (trades == null) {
                System.out.println("DEBUG: trades null!");
                trades = new ArrayList<>();
            }

            System.out.println("DEBUG: Lekérdezvényi Trades szama: " + trades.size());
            if (trades != null && !trades.isEmpty()) {
                for (Trade t : trades) {
                    System.out.println("DEBUG: Trade ID: " + t.getId() + ", Instrument: " + t.getInstrument() + ", Units: " + t.getCurrentUnits());
                }
            }

            model.addAttribute("title", "Nyitott pozíciók");
            model.addAttribute("trades", trades);

        } catch (Exception e) {
            System.out.println("ERROR: Hiba a /forex-poz-nál: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("title", "Nyitott pozíciók");
            model.addAttribute("trades", new ArrayList<>());
            model.addAttribute("error", "Hiba az adatok lekérésekor: " + e.getMessage());
        }

        return "forex/poz"; // poz.html
    }
    @GetMapping("/forex-zar")
    public String forexZarPage(Model model) {
        model.addAttribute("title", "Pozíció zárása");
        return "forex/zar"; // zar.html
    }
    @PostMapping("/forex-zar")
    public String forexZarResult(@RequestParam("tradeId") String tradeId, Model model) {
        StringBuilder strOut = new StringBuilder();

        try {
            // Trade zárása az OANDA API-n keresztül
            ctx.trade.close(new TradeCloseRequest(Config.ACCOUNTID, new TradeSpecifier(tradeId)));

            strOut.append("<div class='alert alert-success'>");
            strOut.append("<h5>Pozíció sikeresen lezárva!</h5>");
            strOut.append("<p><strong>Trade ID:</strong> ").append(tradeId).append("</p>");
            strOut.append("<p><strong>Account ID:</strong> ").append(Config.ACCOUNTID).append("</p>");
            strOut.append("</div>");

            // Sikeres zárás után redirect az account oldalra
            return "redirect:/forex-account";

        } catch (TradeClose404RequestException e) {
            // Specifikus 404 hiba kezelése - nem létező Trade ID
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5>Hiba történt a pozíció zárásakor!</h5>");
            strOut.append("<p><strong>Trade ID:</strong> ").append(tradeId).append("</p>");
            strOut.append("<p class='text-danger'><strong>Nincs ilyen nyitott pozíció!</strong></p>");
            strOut.append("<p>A megadott Trade ID (").append(tradeId).append(") nem található a nyitott pozíciók között.</p>");
            strOut.append("</div>");

        } catch (Exception e) {
            strOut.append("<div class='alert alert-danger'>");
            strOut.append("<h5>Hiba történt a pozíció zárásakor!</h5>");
            strOut.append("<p><strong>Trade ID:</strong> ").append(tradeId).append("</p>");

            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Authorization")) {
                strOut.append("<p>Ellenőrizd, hogy az API token helyesen van-e megadva a Config.java-ban!</p>");
            } else if (errorMsg != null && errorMsg.contains("Throwable#detailMessage")) {
                strOut.append("<p class='text-danger'><strong>Nincs ilyen nyitott pozíció!</strong></p>");
                strOut.append("<p>A megadott Trade ID nem található.</p>");
            } else {
                strOut.append("<p><strong>Hibaüzenet:</strong> ").append(errorMsg != null ? errorMsg : "Ismeretlen hiba").append("</p>");
            }

            strOut.append("</div>");

            model.addAttribute("title", "FOREX Zár - Eredmény");
            model.addAttribute("result", strOut.toString());

        }
        return "forex/zar_result";
    }



}


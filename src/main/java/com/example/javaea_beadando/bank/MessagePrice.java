package com.example.javaea_beadando.bank;

import java.time.LocalDate;

public class MessagePrice {
    private String currency;
    private String startDate;
    private String endDate;

    public MessagePrice() {
        // Alapértelmezett érték: mai dátum
        this.endDate = LocalDate.now().toString();
        // Kezdő dátum alapértelmezetten 30 nappal ezelőtt
        this.startDate = LocalDate.now().minusDays(30).toString();
        // Alapértelmezett deviza
        this.currency = "EUR";
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

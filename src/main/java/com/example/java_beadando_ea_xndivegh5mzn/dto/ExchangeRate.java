package com.example.java_beadando_ea_xndivegh5mzn.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

public class ExchangeRate {
    private String currency;
    private LocalDate date;
    private BigDecimal rate;
    private String unit;

    public ExchangeRate() {}

    public ExchangeRate(String currency, LocalDate date, BigDecimal rate, String unit) {
        this.currency = currency;
        this.date = date;
        this.rate = rate;
        this.unit = unit;
    }

    // Getters and Setters
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "currency='" + currency + '\'' +
                ", date=" + date +
                ", rate=" + rate +
                ", unit='" + unit + '\'' +
                '}';
    }
}

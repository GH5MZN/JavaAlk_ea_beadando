package com.example.javaea_beadando.forex;


public class TradeApplication {
    private String accountId;
    private String accountName;
    private String currency;
    private double balance;
    private double unrealizedPL;
    private double marginUsed;
    private double marginAvailable;
    private int openTradeCount;
    private int openPositionCount;

    public TradeApplication() {
    }

    public TradeApplication(String accountId, String accountName, String currency, double balance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.currency = currency;
        this.balance = balance;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getUnrealizedPL() {
        return unrealizedPL;
    }

    public void setUnrealizedPL(double unrealizedPL) {
        this.unrealizedPL = unrealizedPL;
    }

    public double getMarginUsed() {
        return marginUsed;
    }

    public void setMarginUsed(double marginUsed) {
        this.marginUsed = marginUsed;
    }

    public double getMarginAvailable() {
        return marginAvailable;
    }

    public void setMarginAvailable(double marginAvailable) {
        this.marginAvailable = marginAvailable;
    }

    public int getOpenTradeCount() {
        return openTradeCount;
    }

    public void setOpenTradeCount(int openTradeCount) {
        this.openTradeCount = openTradeCount;
    }

    public int getOpenPositionCount() {
        return openPositionCount;
    }

    public void setOpenPositionCount(int openPositionCount) {
        this.openPositionCount = openPositionCount;
    }
}


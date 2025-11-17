package com.example.javaea_beadando.forex;

public class MessageActPrice {
    private String instrument;
    private String time;
    private double bid;
    private double ask;
    private double spread;

    public MessageActPrice() {
    }

    public MessageActPrice(String instrument, String time, double bid, double ask) {
        this.instrument = instrument;
        this.time = time;
        this.bid = bid;
        this.ask = ask;
        this.spread = ask - bid;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
        this.spread = this.ask - bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
        this.spread = ask - this.bid;
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }
}

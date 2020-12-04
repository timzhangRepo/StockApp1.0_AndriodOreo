package com.timzhang.stockapp;

import androidx.annotation.Nullable;

public class StockItem {
    private String ticker;
    private String info;
    private double current_price;
    private double change;

    public StockItem(String ticker, String info, double current_price, double change) {
        this.ticker = ticker;
        this.info = info;
        this.current_price = current_price;
        this.change = change;
    }


    public String getTicker() {
        return ticker;
    }

    public String getInfo() {
        return info;
    }

    public double getCurrent_price() {
        return current_price;
    }

    public double getChange() {
        return change;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setCurrent_price(double current_price) {
        this.current_price = current_price;
    }

    public void setChange(double change) {
        this.change = change;
    }


}

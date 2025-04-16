package com.example.bai1_nhom;

public class ConversionHistory {
    private double amount;
    private String fromCurrency;
    private double result;
    private String toCurrency;
    private String timestamp;

    public ConversionHistory(double amount, String fromCurrency, double result, String toCurrency, String timestamp) {
        this.amount = amount;
        this.fromCurrency = fromCurrency;
        this.result = result;
        this.toCurrency = toCurrency;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f %s -> %.2f %s", timestamp, amount, fromCurrency, result, toCurrency);
    }
}
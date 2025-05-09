package com.example.myapplication;

public class ForecastItem {
    String date;
    double maxTemp;
    double minTemp;
    String conditionText;
    String iconUrl;

    public ForecastItem(String date, double maxTemp, double minTemp, String conditionText, String iconUrl) {
        this.date = date;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.conditionText = conditionText;
        this.iconUrl = iconUrl;
    }
}

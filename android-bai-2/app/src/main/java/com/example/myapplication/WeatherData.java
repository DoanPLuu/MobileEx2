package com.example.myapplication;

public class WeatherData {
    private double currentTemp;
    private String currentCondition;
    private int humidity;
    private double windSpeed;
    private double precipMm;
    private double feelsLike;
    private double visibility;
    private double pressure;
    private double uvIndex;
    private String iconUrl;

    public WeatherData(double currentTemp, String currentCondition, int humidity, double windSpeed,
                      double precipMm, double feelsLike, double visibility, double pressure,
                      double uvIndex, String iconUrl) {
        this.currentTemp = currentTemp;
        this.currentCondition = currentCondition;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipMm = precipMm;
        this.feelsLike = feelsLike;
        this.visibility = visibility;
        this.pressure = pressure;
        this.uvIndex = uvIndex;
        this.iconUrl = iconUrl;
    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public String getCurrentCondition() {
        return currentCondition;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getPrecipMm() {
        return precipMm;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public double getVisibility() {
        return visibility;
    }

    public double getPressure() {
        return pressure;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}

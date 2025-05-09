package com.example.myapplication;

public class HourlyForecastItem {
    private String time;
    private double temp;
    private String condition;
    private String iconUrl;
    private double rainMm;
    private double windSpeed;

    public HourlyForecastItem(String time, double temp, String condition, String iconUrl, double rainMm, double windSpeed) {
        this.time = time;
        this.temp = temp;
        this.condition = condition;
        this.iconUrl = iconUrl;
        this.rainMm = rainMm;
        this.windSpeed = windSpeed;
    }

    public String getTime() {
        return time;
    }

    public double getTemp() {
        return temp;
    }

    public String getCondition() {
        return condition;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public double getRainMm() {
        return rainMm;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}

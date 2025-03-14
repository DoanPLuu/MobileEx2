package com.example.restweather;

import java.util.List;

public class Weather {
    private Main main;
    private Wind wind;
    private List<WeatherDescription> weather;

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public List<WeatherDescription> getWeather() {
        return weather;
    }

    public class Main {
        private double temp;
        private double temp_min;
        private double temp_max;
        private int humidity;

        public double getTemp() { return temp; }
        public double getTempMin() { return temp_min; }
        public double getTempMax() { return temp_max; }
        public int getHumidity() { return humidity; }
    }

    public class Wind {
        private double speed;

        public double getSpeed() { return speed; }
    }

    public class WeatherDescription {
        private String description;

        public String getDescription() { return description; }
    }
}

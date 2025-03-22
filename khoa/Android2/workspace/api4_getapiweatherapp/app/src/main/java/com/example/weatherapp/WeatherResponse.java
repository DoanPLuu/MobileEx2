package com.example.weatherapp;

import java.util.List;

public class WeatherResponse {
    private Main main;
    private List<Weather> weather;

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public class Main {
        private float temp;
        private int humidity;

        public float getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public class Weather {
        private String main; // Thêm trường main
        private String description;

        public String getMain() { // Thêm phương thức getMain
            return main;
        }

        public String getDescription() {
            return description;
        }
    }
}
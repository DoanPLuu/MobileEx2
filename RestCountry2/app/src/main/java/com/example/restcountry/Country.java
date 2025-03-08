package com.example.restcountry;

public class Country {
    private String name;
    private String capital;
    private String region;

    public Country(String name, String capital, String region) {
        this.name = name;
        this.capital = capital;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public String getCapital() {
        return capital;
    }

    public String getRegion() {
        return region;
    }
}
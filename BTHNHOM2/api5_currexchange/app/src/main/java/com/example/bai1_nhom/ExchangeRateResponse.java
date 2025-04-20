package com.example.bai1_nhom;


import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRateResponse {
    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("conversion_rates")
    private Map<String, Double> conversionRates;

    public String getBaseCode() {
        return baseCode;
    }

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }
}
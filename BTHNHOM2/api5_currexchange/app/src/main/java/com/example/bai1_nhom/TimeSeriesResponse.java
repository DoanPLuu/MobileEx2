package com.example.bai1_nhom;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class TimeSeriesResponse {
    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("rates")
    private Map<String, Map<String, Double>> rates;

    public Map<String, Map<String, Double>> getRates() {
        return rates;
    }
}

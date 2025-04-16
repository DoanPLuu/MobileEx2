package com.example.bai1_nhom;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    Call<ExchangeRateResponse> getLatestRates(
            @Path("apiKey") String apiKey,
            @Path("baseCurrency") String baseCurrency
    );


    @GET("v6/{apiKey}/timeseries/{baseCurrency}")
    Call<TimeSeriesResponse> getTimeSeries(
            @Path("apiKey") String apiKey,
            @Path("baseCurrency") String baseCurrency,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("symbols") String symbols
    );

}
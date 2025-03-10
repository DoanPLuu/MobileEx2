package com.example.restcountry;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NetworkUtils {

    private static final String API_URL = "https://restcountries.com/v3.1/all";

    public static ArrayList<Country> fetchCountries() {
        ArrayList<Country> countryList = new ArrayList<>();

        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONArray jsonArray = new JSONArray(result.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject countryObj = jsonArray.getJSONObject(i);
                String name = countryObj.getJSONObject("name").getString("common");
                String capital = countryObj.has("capital") ? countryObj.getJSONArray("capital").getString(0) : "N/A";
                String region = countryObj.getString("region");

                countryList.add(new Country(name, capital, region));
            }

        } catch (Exception e) {
            Log.e("NetworkUtils", "Error fetching countries", e);
        }

        return countryList;
    }
}
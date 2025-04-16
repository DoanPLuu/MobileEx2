package com.example.bai1_nhom;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryStorage {
    private static HistoryStorage instance;
    private SharedPreferences prefs;
    private Gson gson;
    private static final String PREF_NAME = "CurrencyConverter";
    private static final String HISTORY_KEY = "conversion_history";

    private HistoryStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static HistoryStorage getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryStorage(context);
        }
        return instance;
    }

    public void addHistory(ConversionHistory history) {
        List<ConversionHistory> historyList = getHistory();
        historyList.add(history);
        String json = gson.toJson(historyList);
        prefs.edit().putString(HISTORY_KEY, json).apply();
    }

    public List<ConversionHistory> getHistory() {
        String json = prefs.getString(HISTORY_KEY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ConversionHistory>>(){}.getType();
        return gson.fromJson(json, type);
    }
}
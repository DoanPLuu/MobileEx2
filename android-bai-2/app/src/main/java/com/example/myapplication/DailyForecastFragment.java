package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DailyForecastFragment extends Fragment {

    private RecyclerView dailyRecyclerView;
    private ForecastAdapter adapter;
    private List<ForecastItem> forecastList;
    private boolean isCelsius = true;

    public DailyForecastFragment() {
        // Required empty public constructor
    }

    public static DailyForecastFragment newInstance() {
        return new DailyForecastFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dailyRecyclerView = view.findViewById(R.id.dailyRecyclerView);
        forecastList = new ArrayList<>();
        
        // Đồng bộ trạng thái đơn vị với MainActivity
        if (getActivity() instanceof MainActivity) {
            isCelsius = ((MainActivity) getActivity()).isCelsius();
        }
        
        adapter = new ForecastAdapter(forecastList, isCelsius);

        dailyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyRecyclerView.setAdapter(adapter);
    }

    public void updateDailyForecast(List<ForecastItem> forecast) {
        if (forecastList != null && adapter != null) {
            forecastList.clear();
            forecastList.addAll(forecast);
            adapter.notifyDataSetChanged();
        }
    }
    
    public void setTemperatureUnit(boolean celsius) {
        if (this.isCelsius != celsius) {
            this.isCelsius = celsius;
            if (adapter != null) {
                adapter.setTemperatureUnit(celsius);
                adapter.notifyDataSetChanged();
            }
        }
    }
}

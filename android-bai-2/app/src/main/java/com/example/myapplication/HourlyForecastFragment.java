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

public class HourlyForecastFragment extends Fragment {

    private RecyclerView hourlyRecyclerView;
    private HourlyForecastAdapter adapter;
    private List<HourlyForecastItem> hourlyForecastList;
    private boolean isCelsius = true;
    public HourlyForecastFragment() {
        // Required empty public constructor
    }

    public static HourlyForecastFragment newInstance() {
        return new HourlyForecastFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hourly_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hourlyRecyclerView = view.findViewById(R.id.hourlyRecyclerView);
        hourlyForecastList = new ArrayList<>();

        // Đồng bộ trạng thái đơn vị với MainActivity
        if (getActivity() instanceof MainActivity) {
            isCelsius = ((MainActivity) getActivity()).isCelsius();
        }

        adapter = new HourlyForecastAdapter(hourlyForecastList, isCelsius);

        hourlyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        hourlyRecyclerView.setAdapter(adapter);
    }

    public void updateHourlyForecast(List<HourlyForecastItem> hourlyForecast) {
        if (hourlyForecastList != null && adapter != null) {
            hourlyForecastList.clear();
            hourlyForecastList.addAll(hourlyForecast);
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

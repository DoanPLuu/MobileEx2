package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 4;
    
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return CurrentWeatherFragment.newInstance();
            case 1:
                return HourlyForecastFragment.newInstance();
            case 2:
                return DailyForecastFragment.newInstance();
            case 3:
                return WeatherMapFragment.newInstance();
            default:
                return CurrentWeatherFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}

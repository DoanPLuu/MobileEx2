package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class CurrentWeatherFragment extends Fragment {

    private TextView currentTempText, currentConditionText, humidityText, windText, rainText;
    private TextView feelsLikeText, visibilityText, pressureText, uvIndexText;
    private Switch tempUnitSwitch;
    private WeatherData weatherData;
    private boolean isCelsius = true;

    public CurrentWeatherFragment() {
        // Required empty public constructor
    }

    public static CurrentWeatherFragment newInstance() {
        return new CurrentWeatherFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentTempText = view.findViewById(R.id.currentTempText);
        currentConditionText = view.findViewById(R.id.currentConditionText);
        humidityText = view.findViewById(R.id.humidityText);
        windText = view.findViewById(R.id.windText);
        rainText = view.findViewById(R.id.rainText);
        feelsLikeText = view.findViewById(R.id.feelsLikeText);
        visibilityText = view.findViewById(R.id.visibilityText);
        pressureText = view.findViewById(R.id.pressureText);
        uvIndexText = view.findViewById(R.id.uvIndexText);
        tempUnitSwitch = view.findViewById(R.id.tempUnitSwitch);
        
        tempUnitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCelsius = !isChecked;
            updateUI();
        });

        // If we already have weather data, update the UI
        if (weatherData != null) {
            updateUI();
        }

        tempUnitSwitch = view.findViewById(R.id.tempUnitSwitch);

        // Đồng bộ trạng thái switch với MainActivity
        if (getActivity() instanceof MainActivity) {
            isCelsius = ((MainActivity) getActivity()).isCelsius();
            tempUnitSwitch.setChecked(!isCelsius); // Checked = F, Unchecked = C
        }

        tempUnitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCelsius = !isChecked;
            updateUI();

            // Thông báo cho MainActivity về thay đổi
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setCelsius(isCelsius);
            }
        });
    }

    // Thêm phương thức để cập nhật đơn vị từ bên ngoài
    public void setTemperatureUnit(boolean celsius) {
        if (this.isCelsius != celsius) {
            this.isCelsius = celsius;
            tempUnitSwitch.setChecked(!celsius); // Cập nhật UI switch
            updateUI(); // Cập nhật hiển thị nhiệt độ
        }
    }
    public void updateWeatherData(WeatherData data) {
        this.weatherData = data;
        if (isAdded() && currentTempText != null) {
            updateUI();
        }
    }

    private void updateUI() {
        if (weatherData == null) return;

        double tempToShow = isCelsius ? weatherData.getCurrentTemp() : celsiusToFahrenheit(weatherData.getCurrentTemp());
        double feelsLikeToShow = isCelsius ? weatherData.getFeelsLike() : celsiusToFahrenheit(weatherData.getFeelsLike());
        String unit = isCelsius ? "°C" : "°F";
        
        currentTempText.setText(String.format("%.1f%s", tempToShow, unit));
        currentConditionText.setText(weatherData.getCurrentCondition());
        humidityText.setText(weatherData.getHumidity() + "%");
        windText.setText(weatherData.getWindSpeed() + " km/h");
        rainText.setText(weatherData.getPrecipMm() + " mm");
        feelsLikeText.setText(String.format("%.1f%s", feelsLikeToShow, unit));
        visibilityText.setText(weatherData.getVisibility() + " km");
        pressureText.setText(weatherData.getPressure() + " hPa");
        uvIndexText.setText(String.valueOf(weatherData.getUvIndex()));
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }
}

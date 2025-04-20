package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText cityInput;
    private TextView weatherInfo;
    private Button getWeatherBtn;
    private LottieAnimationView lottieAnimation;
    private String API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        weatherInfo = findViewById(R.id.weatherInfo);
        getWeatherBtn = findViewById(R.id.getWeatherBtn);
        lottieAnimation = findViewById(R.id.lottieAnimation);

        getWeatherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityInput.getText().toString();
                if (!city.isEmpty()) {
                    getWeather(city);
                }
            }
        });
    }

    private void getWeather(String city) {
        lottieAnimation.setVisibility(View.VISIBLE);
        lottieAnimation.setAnimation(R.raw.weather_animation);
        lottieAnimation.playAnimation();
        weatherInfo.setVisibility(View.GONE);

        WeatherAPI weatherAPI = RetrofitClient.getClient().create(WeatherAPI.class);
        Call<WeatherResponse> call = weatherAPI.getWeather(city, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weather = response.body();
                    if (weather != null) {
                        String weatherMain = weather.getWeather().get(0).getMain().toLowerCase();
                        String weatherDescription = weather.getWeather().get(0).getDescription().toLowerCase();
                        String info = "Nhiệt độ: " + weather.getMain().getTemp() + "°C\n"
                                + "Độ ẩm: " + weather.getMain().getHumidity() + "%\n"
                                + "Mô tả: " + weather.getWeather().get(0).getDescription();

                        setWeatherAnimation(weatherMain, weatherDescription);
                        weatherInfo.setAlpha(0f);
                        weatherInfo.setText(info);
                        weatherInfo.setVisibility(View.VISIBLE);
                        weatherInfo.animate().alpha(1f).setDuration(500).start();
                    }
                } else {
                    lottieAnimation.setVisibility(View.GONE);
                    lottieAnimation.cancelAnimation();
                    weatherInfo.setText("Không tìm thấy dữ liệu cho thành phố này!");
                    weatherInfo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                lottieAnimation.setVisibility(View.GONE);
                lottieAnimation.cancelAnimation();
                weatherInfo.setText("Không lấy được dữ liệu!");
                weatherInfo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setWeatherAnimation(String weatherMain, String weatherDescription) {
        lottieAnimation.cancelAnimation();

        if (weatherMain.contains("thunderstorm") || weatherDescription.contains("storm")) {
            lottieAnimation.setAnimation(R.raw.storm);
        } else if (weatherMain.contains("clear")) {
            lottieAnimation.setAnimation(R.raw.sunny);
        } else if (weatherMain.contains("rain") || weatherDescription.contains("rain")) {
            lottieAnimation.setAnimation(R.raw.rain);
        } else if (weatherMain.contains("clouds") || weatherDescription.contains("cloud")) {
            lottieAnimation.setAnimation(R.raw.cloudy);
        } else {
            lottieAnimation.setAnimation(R.raw.weather_animation);
        }

        lottieAnimation.setVisibility(View.VISIBLE);
        lottieAnimation.playAnimation();
    }
}
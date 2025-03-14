package com.example.restweather;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private EditText editTextCity;
    private TextView textViewResult;
    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCity = findViewById(R.id.editTextCity);
        textViewResult = findViewById(R.id.textViewResult);
        Button buttonFetch = findViewById(R.id.buttonFetch);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);

        buttonFetch.setOnClickListener(view -> {
            String cityName = editTextCity.getText().toString().trim();

            if (!cityName.isEmpty()) {
                Call<Weather> call = weatherService.getWeatherData(cityName, API_KEY, "metric");

                call.enqueue(new Callback<Weather>() {
                    @Override
                    public void onResponse(Call<Weather> call, Response<Weather> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Weather weatherData = response.body();
                            String weatherInfo = String.format(
                                    "Thành phố: %s\n" +
                                            "Nhiệt độ hiện tại: %.1f°C\n" +
                                            "Nhiệt độ thấp nhất: %.1f°C\n" +
                                            "Nhiệt độ cao nhất: %.1f°C\n" +
                                            "Độ ẩm: %d%%\n" +
                                            "Tốc độ gió: %.1f m/s\n" +
                                            "Mô tả: %s",
                                    cityName,
                                    weatherData.getMain().getTemp(),
                                    weatherData.getMain().getTempMin(),
                                    weatherData.getMain().getTempMax(),
                                    weatherData.getMain().getHumidity(),
                                    weatherData.getWind().getSpeed(),
                                    weatherData.getWeather().get(0).getDescription()
                            );

                            textViewResult.setText(weatherInfo);
                        } else {
                            textViewResult.setText("Không tìm thấy thông tin thời tiết!");
                        }
                    }

                    @Override
                    public void onFailure(Call<Weather> call, Throwable t) {
                        textViewResult.setText("Lỗi: " + t.getMessage());
                    }
                });
            } else {
                textViewResult.setText("Vui lòng nhập tên thành phố!");
            }
        });

    }
}

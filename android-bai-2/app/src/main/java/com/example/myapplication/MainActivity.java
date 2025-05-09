package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 1;
    private TextView locationText, weatherText;
    private Button refreshButton;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private List<ForecastItem> forecastList;
    private List<HourlyForecastItem> hourlyForecastList;
    private WeatherData currentWeatherData;
    private double currentLat, currentLon;

    // Fragment references
    private CurrentWeatherFragment currentWeatherFragment;
    private HourlyForecastFragment hourlyForecastFragment;
    private DailyForecastFragment dailyForecastFragment;
    private WeatherMapFragment weatherMapFragment;
    private boolean isCelsius = true; // Mặc định là độ C
    private final String WEATHER_API_KEY = "bc5b6a2e6b924158b43125707250905";
    private final String OPENWEATHERMAP_KEY = "c4090fb2694aa848fe88b3d88a37e6af";

    // Thêm getter và setter
    public boolean isCelsius() {
        return isCelsius;
    }
    public void setCelsius(boolean celsius) {
        isCelsius = celsius;
        // Cập nhật tất cả các fragment khi đơn vị thay đổi
        updateAllFragmentsTemperatureUnit();
    }

    private void updateAllFragmentsTemperatureUnit() {
        // Cập nhật fragment hiện tại
        if (getCurrentWeatherFragment() != null) {
            getCurrentWeatherFragment().setTemperatureUnit(isCelsius);
        }
        
        // Cập nhật fragment theo giờ
        if (getHourlyForecastFragment() != null) {
            getHourlyForecastFragment().setTemperatureUnit(isCelsius);
        }
        
        // Cập nhật fragment theo ngày
        if (getDailyForecastFragment() != null) {
            getDailyForecastFragment().setTemperatureUnit(isCelsius);
        }
    } // Thêm dấu đóng ngoặc ở đây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        locationText = findViewById(R.id.locationText);
        weatherText = findViewById(R.id.weatherText);
        refreshButton = findViewById(R.id.refreshButton);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize data lists
        forecastList = new ArrayList<>();
        hourlyForecastList = new ArrayList<>();

        // Set up ViewPager and TabLayout
        setupViewPager();

        // Set up refresh button
        refreshButton.setOnClickListener(v -> fetchWeatherData());

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // Check location permission
        getLocationPermission();

        // Schedule periodic weather checks
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            scheduleWeatherWorker();
        }
    }

    private void scheduleWeatherWorker() {
        PeriodicWorkRequest weatherRequest = new PeriodicWorkRequest.Builder(
                WeatherCheckWorker.class,
                15, TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "weather_check_work",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherRequest
        );
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            fetchWeatherData();
        }
    }

    private void setupViewPager() {
        // Set up ViewPager with adapter
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        
        // Vô hiệu hóa vuốt khi đang ở tab map
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Vô hiệu hóa vuốt khi đang ở tab map (position 3)
                viewPager.setUserInputEnabled(position != 3);
            }
        });

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Hiện tại");
                    break;
                case 1:
                    tab.setText("Theo giờ");
                    break;
                case 2:
                    tab.setText("7 ngày");
                    break;
                case 3:
                    tab.setText("Bản đồ");
                    break;
            }
        }).attach();
    }

    @SuppressLint("MissingPermission")
    private void fetchWeatherData() {
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(this);
        fusedClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                locationText.setText("Vị trí: " + currentLat + ", " + currentLon);
                getWeatherFromAPI(currentLat, currentLon);

                // Update map fragment with location
                if (weatherMapFragment != null) {
                    weatherMapFragment.updateLocation(currentLat, currentLon);
                }
            } else {
                locationText.setText("Không lấy được vị trí.");
            }
        });
    }

    private void getWeatherFromAPI(double lat, double lon) {
        // Update to fetch 7 days of forecast
        String url = "https://api.weatherapi.com/v1/forecast.json?key=" + WEATHER_API_KEY +
                "&q=" + lat + "," + lon + "&days=7&lang=vi";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject location = response.getJSONObject("location");
                        JSONObject current = response.getJSONObject("current");
                        JSONArray forecastday = response.getJSONObject("forecast").getJSONArray("forecastday");
                        JSONObject today = forecastday.getJSONObject(0).getJSONObject("day");

                        String city = location.getString("name");
                        double tempC = current.getDouble("temp_c");
                        double feelsLikeC = current.getDouble("feelslike_c");
                        int humidity = current.getInt("humidity");
                        double windKph = current.getDouble("wind_kph");
                        double precipMm = current.getDouble("precip_mm");
                        double visKm = current.getDouble("vis_km");
                        double pressureMb = current.getDouble("pressure_mb");
                        double uv = current.getDouble("uv");
                        String condition = current.getJSONObject("condition").getString("text");
                        String iconUrl = "https:" + current.getJSONObject("condition").getString("icon");

                        // Update basic weather info in the header
                        weatherText.setText("Thành phố: " + city +
                                "\nNhiệt độ hiện tại: " + tempC + "°C" +
                                "\nTrạng thái: " + condition);

                        // Create WeatherData object for current weather
                        currentWeatherData = new WeatherData(tempC, condition, humidity, windKph,
                                precipMm, feelsLikeC, visKm, pressureMb, uv, iconUrl);

                        // Update current weather fragment if available
                        if (getCurrentWeatherFragment() != null) {
                            getCurrentWeatherFragment().updateWeatherData(currentWeatherData);
                        }

                        // Check for extreme weather conditions and show notification
                        checkWeatherConditionsForAlert(city, tempC, condition, precipMm);

                        // Process daily forecast data
                        processDailyForecast(forecastday);

                        // Process hourly forecast data (from the first day)
                        processHourlyForecast(forecastday.getJSONObject(0).getJSONArray("hour"));

                    } catch (Exception e) {
                        e.printStackTrace();
                        weatherText.setText("❌ Lỗi khi đọc dữ liệu thời tiết.");
                    }
                },
                error -> {
                    error.printStackTrace();
                    weatherText.setText("❌ Không lấy được dữ liệu thời tiết.");
                });

        queue.add(request);
    }

    // Helper methods for processing weather data
    private void checkWeatherConditionsForAlert(String city, double tempC, String condition, double precipMm) {
        // Check for extreme temperature
        if (tempC >= 35 || tempC <= 15) {
            showWeatherNotification("⚠️ Cảnh báo nhiệt độ",
                    "Nhiệt độ hiện tại tại " + city + " là " + tempC + "°C – bất thường!");
        }

        // Check for heavy rain
        if (precipMm > 10) {
            showWeatherNotification("⚠️ Cảnh báo mưa lớn",
                    "Lượng mưa hiện tại tại " + city + " là " + precipMm + " mm – mưa lớn!");
        }

        // Check for bad weather conditions
        if (condition.toLowerCase().contains("mưa") ||
            condition.toLowerCase().contains("bão") ||
            condition.toLowerCase().contains("giông")) {
            showWeatherNotification("⚠️ Cảnh báo thời tiết xấu",
                    "Thời tiết hiện tại tại " + city + ": " + condition);
        }
    }

    private void processDailyForecast(JSONArray forecastday) {
        try {
            forecastList.clear();

            for (int i = 0; i < forecastday.length(); i++) {
                JSONObject day = forecastday.getJSONObject(i);
                String date = day.getString("date");
                JSONObject dayInfo = day.getJSONObject("day");
                double maxTemp = dayInfo.getDouble("maxtemp_c");
                double minTemp = dayInfo.getDouble("mintemp_c");
                String conditionText = dayInfo.getJSONObject("condition").getString("text");
                String icon = dayInfo.getJSONObject("condition").getString("icon");

                forecastList.add(new ForecastItem(date, maxTemp, minTemp, conditionText, icon));
            }

            // Update daily forecast fragment if available
            if (getDailyForecastFragment() != null) {
                getDailyForecastFragment().updateDailyForecast(forecastList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processHourlyForecast(JSONArray hours) {
        try {
            hourlyForecastList = new ArrayList<>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (int i = 0; i < hours.length(); i++) {
                JSONObject hour = hours.getJSONObject(i);
                String timeStr = hour.getString("time");
                Date time = inputFormat.parse(timeStr);
                String formattedTime = outputFormat.format(time);

                double temp = hour.getDouble("temp_c");
                String condition = hour.getJSONObject("condition").getString("text");
                String iconUrl = hour.getJSONObject("condition").getString("icon");
                double rainMm = hour.getDouble("precip_mm");
                double windKph = hour.getDouble("wind_kph");

                hourlyForecastList.add(new HourlyForecastItem(formattedTime, temp, condition, iconUrl, rainMm, windKph));
            }

            // Update hourly forecast fragment if available
            if (getHourlyForecastFragment() != null) {
                getHourlyForecastFragment().updateHourlyForecast(hourlyForecastList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper methods to get fragment references
    private CurrentWeatherFragment getCurrentWeatherFragment() {
        if (currentWeatherFragment == null) {
            currentWeatherFragment = (CurrentWeatherFragment) getSupportFragmentManager()
                    .findFragmentByTag("f0");
        }
        return currentWeatherFragment;
    }

    private HourlyForecastFragment getHourlyForecastFragment() {
        if (hourlyForecastFragment == null) {
            hourlyForecastFragment = (HourlyForecastFragment) getSupportFragmentManager()
                    .findFragmentByTag("f1");
        }
        return hourlyForecastFragment;
    }

    private DailyForecastFragment getDailyForecastFragment() {
        if (dailyForecastFragment == null) {
            dailyForecastFragment = (DailyForecastFragment) getSupportFragmentManager()
                    .findFragmentByTag("f2");
        }
        return dailyForecastFragment;
    }

    private WeatherMapFragment getWeatherMapFragment() {
        if (weatherMapFragment == null) {
            weatherMapFragment = (WeatherMapFragment) getSupportFragmentManager()
                    .findFragmentByTag("f3");
        }
        return weatherMapFragment;
    }

    // Initialize map with common settings
    public void initializeMap(GoogleMap googleMap) {
        LatLng location = new LatLng(currentLat, currentLon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 8));

        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        googleMap.setOnMapClickListener(latLng -> {
            double clickedLat = latLng.latitude;
            double clickedLon = latLng.longitude;

            getWeatherAtLocation(googleMap, clickedLat, clickedLon);
            loadNearbyCitiesFromGeoNames(googleMap, clickedLat, clickedLon);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        });
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeatherData();
        } else {
            Toast.makeText(this, "Không có quyền vị trí.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addWeatherMarkerToMap(GoogleMap googleMap, double lat, double lon, String iconUrl, String title,String snippet) {
        Picasso.get().load(iconUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            }



            @Override public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
            @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
        });
    }
    private void getWeatherAtLocation(GoogleMap googleMap, double lat, double lon) {
        // Update to fetch 7 days of forecast
        String url = "https://api.weatherapi.com/v1/forecast.json?key=" + WEATHER_API_KEY +
                "&q=" + lat + "," + lon + "&days=7&lang=vi";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject location = response.getJSONObject("location");
                        JSONObject current = response.getJSONObject("current");
                        JSONArray forecastday = response.getJSONObject("forecast").getJSONArray("forecastday");
                        JSONObject today = forecastday.getJSONObject(0).getJSONObject("day");

                        String city = location.getString("name");
                        double tempC = current.getDouble("temp_c");
                        double feelsLikeC = current.getDouble("feelslike_c");
                        int humidity = current.getInt("humidity");
                        double windKph = current.getDouble("wind_kph");
                        double precipMm = current.getDouble("precip_mm");
                        double visKm = current.getDouble("vis_km");
                        double pressureMb = current.getDouble("pressure_mb");
                        double uv = current.getDouble("uv");
                        String condition = current.getJSONObject("condition").getString("text");
                        String iconUrl = "https:" + current.getJSONObject("condition").getString("icon");

                        // Update basic weather info in the header
                        weatherText.setText("📍 " + city +
                                "\nNhiệt độ: " + tempC + "°C" +
                                "\nTrạng thái: " + condition);

                        // Create marker info
                        String title = city + ": " + condition;
                        StringBuilder snippet = new StringBuilder();
                        double maxTemp = today.optDouble("maxtemp_c", 0.0);
                        double minTemp = today.optDouble("mintemp_c", 0.0);
                        snippet.append("🌡 Nhiệt độ: ").append(minTemp).append("°C ~ ").append(maxTemp).append("°C");
                        snippet.append("\n☔ Lượng mưa: ").append(precipMm).append(" mm");

                        // Add marker to map
                        addWeatherMarkerToMap(googleMap, lat, lon, iconUrl, title, String.valueOf(snippet));

                        // Create WeatherData object for current weather
                        currentWeatherData = new WeatherData(tempC, condition, humidity, windKph,
                                precipMm, feelsLikeC, visKm, pressureMb, uv, iconUrl);

                        // Update current weather fragment if available
                        if (getCurrentWeatherFragment() != null) {
                            getCurrentWeatherFragment().updateWeatherData(currentWeatherData);
                        }

                        // Check for extreme weather conditions and show notification
                        checkWeatherConditionsForAlert(city, tempC, condition, precipMm);

                        // Process daily forecast data
                        processDailyForecast(forecastday);

                        // Process hourly forecast data (from the first day)
                        processHourlyForecast(forecastday.getJSONObject(0).getJSONArray("hour"));

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "❌ Lỗi khi đọc thời tiết vị trí chọn", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "❌ Không lấy được dữ liệu dự báo", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }


    private void loadNearbyCitiesFromGeoNames(GoogleMap googleMap, double lat, double lon) {
        String geoNamesUrl = "http://api.geonames.org/citiesJSON?north=" + (lat + 0.3) +
                "&south=" + (lat - 0.3) +
                "&east=" + (lon + 0.3) +
                "&west=" + (lon - 0.3) +
                "&username=hiep12322222";


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, geoNamesUrl, null,
                response -> {
                    Log.d("GEONAMES_RESPONSE", response.toString());
                    try {
                        JSONArray geonames = response.getJSONArray("geonames");
                        List<CityInfo> cities = new ArrayList<>();

                        for (int i = 0; i < Math.min(geonames.length(), 10); i++) {
                            JSONObject obj = geonames.getJSONObject(i);
                            String name = obj.getString("name");
                            double cityLat = obj.getDouble("lat");
                            double cityLon = obj.getDouble("lng");

                            cities.add(new CityInfo(name, cityLat, cityLon));
                        }


                        loadCityWeatherMarkers(googleMap, cities);

                    } catch (Exception e) {
                        Log.e("GEONAMES_ERROR", "Parse GeoNames failed: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("GEONAMES_ERROR", "GeoNames API failed: " + error.toString());
                });

        queue.add(request);
    }

    private void loadCityWeatherMarkers(GoogleMap googleMap, List<CityInfo> cities) {
        for (CityInfo city : cities) {
            String url = "https://api.weatherapi.com/v1/forecast.json?key=" + WEATHER_API_KEY +
                    "&q=" + city.lat + "," + city.lon + "&days=1&lang=vi";

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONObject forecast = response.getJSONObject("forecast");
                            JSONArray forecastday = forecast.getJSONArray("forecastday");
                            JSONObject today = forecastday.getJSONObject(0).getJSONObject("day");

                            String condition = today.getJSONObject("condition").getString("text");
                            String iconUrl = "https:" + today.getJSONObject("condition").getString("icon");
                            double rainMM = today.optDouble("totalprecip_mm", 0.0);
                            double maxTemp = today.optDouble("maxtemp_c", 0.0);
                            double minTemp = today.optDouble("mintemp_c", 0.0);


                            String title = city.name + ": " + condition;


                            StringBuilder snippet = new StringBuilder();
                            snippet.append("🌡 Nhiệt độ: ").append(minTemp).append("°C ~ ").append(maxTemp).append("°C");

                                snippet.append("\n☔ Lượng mưa: ").append(rainMM).append(" mm");


                            addWeatherMarkerToMap(googleMap, city.lat, city.lon, iconUrl, title, snippet.toString());

                        } catch (Exception e) {
                            Log.e("WEATHER_CITY_ERROR", e.toString());
                        }
                    },
                    error -> Log.e("WEATHER_CITY_ERROR", error.toString()));

            queue.add(request);
        }

    }
    @SuppressLint("NotificationPermission")
    private void showWeatherNotification(String title, String content) {
        String CHANNEL_ID = "weather_alert_channel";

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d("Notification","calling");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo thời tiết",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo khi thời tiết bất thường");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}

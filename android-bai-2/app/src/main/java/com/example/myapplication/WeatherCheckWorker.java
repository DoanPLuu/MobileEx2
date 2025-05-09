// 1. File: WeatherCheckWorker.java
package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONObject;

public class WeatherCheckWorker extends Worker {

    private static final String CHANNEL_ID = "weather_alert_channel";
    private static final String WEATHER_API_KEY = "544ba2f57ce243acbab30912252901";
    private Context context;

    public WeatherCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure();
        }

        try {
            Location location = Tasks.await(fusedClient.getLastLocation());

            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                // Use forecast API to get more data
                String url = "https://api.weatherapi.com/v1/forecast.json?key=" + WEATHER_API_KEY +
                        "&q=" + lat + "," + lon + "&days=1&lang=vi";

                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        response -> {
                            try {
                                JSONObject locationObj  = response.getJSONObject("location");
                                String city = locationObj .getString("name");
                                JSONObject current = response.getJSONObject("current");
                                double temp = current.getDouble("temp_c");
                                double precipMm = current.getDouble("precip_mm");
                                String condition = current.getJSONObject("condition").getString("text");

                                // Check for extreme temperature
                                if (temp >= 35 || temp <= 15) {
                                    sendNotification("⚠️ Cảnh báo nhiệt độ",
                                            "Nhiệt độ hiện tại tại " + city + ": " + temp + "°C - " + condition);
                                }

                                // Check for heavy rain
                                if (precipMm > 10) {
                                    sendNotification("⚠️ Cảnh báo mưa lớn",
                                            "Lượng mưa hiện tại tại " + city + ": " + precipMm + " mm");
                                }

                                // Check for bad weather conditions
                                if (condition.toLowerCase().contains("mưa") ||
                                    condition.toLowerCase().contains("bão") ||
                                    condition.toLowerCase().contains("giông")) {
                                    sendNotification("⚠️ Cảnh báo thời tiết xấu",
                                            "Thời tiết hiện tại tại " + city + ": " + condition);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> error.printStackTrace());

                queue.add(request);
            } else {
                sendNotification("🌐 Không lấy được vị trí", "Không thể truy cập vị trí hiện tại.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

        return Result.success();
    }


    private void sendNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Cảnh báo thời tiết", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo khi thời tiết bất thường");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(2001, builder.build());
    }
}

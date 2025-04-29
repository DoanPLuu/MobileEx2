package com.example.bai3_nhom;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SongControlActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MediaPlayer mediaPlayer;
    private static final float TILT_THRESHOLD = 3.0f; // Giảm ngưỡng để nhạy hơn
    private long lastActionTime = 0;
    private static final long ACTION_DELAY = 1000; // 1 giây chờ

    // Danh sách bài hát (ID tài nguyên trong res/raw)
    private int[] songs = {R.raw.aloalo, R.raw.diquadeo, R.raw.trinh}; // Thay song2, song3 bằng tên file thực
    private int currentSongIndex = 0; // Chỉ số bài hát hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Cảm biến gia tốc không có sẵn!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Khởi tạo MediaPlayer với bài hát đầu tiên
        playSong(currentSongIndex);
    }

    private void playSong(int songIndex) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, songs[songIndex]);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Đang phát bài " + (songIndex + 1), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Không thể phát bài hát!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            Log.d("SensorDebug", "X value: " + x); // Debug giá trị x

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActionTime < ACTION_DELAY) {
                return;
            }

            // Xoay trái (X dương) để chuyển bài tiếp theo
            if (x > TILT_THRESHOLD) {
                lastActionTime = currentTime;
                skipToNext();
            }
            // Xoay phải (X âm) để quay lại bài trước
            else if (x < -TILT_THRESHOLD) {
                lastActionTime = currentTime;
                skipToPrevious();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void skipToNext() {
        if (currentSongIndex < songs.length - 1) {
            currentSongIndex++;
            playSong(currentSongIndex);
        } else {
            Toast.makeText(this, "Đã ở bài cuối cùng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void skipToPrevious() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            playSong(currentSongIndex);
        } else {
            Toast.makeText(this, "Đã ở bài đầu tiên!", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.bai3_nhom;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VolumeControlActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AudioManager audioManager;
    private TextView statusTextView;
    private TextView currentVolumeTextView;

    private static final float INCREASE_TILT_THRESHOLD = 5.0f; // Ngưỡng nghiêng trái để tăng âm lượng
    private static final float DECREASE_TILT_THRESHOLD = -10.0f; // Ngưỡng nghiêng phải sâu hơn để giảm âm lượng
    private long lastActionTime = 0;
    private static final long ACTION_DELAY = 500; // 0.5 giây chờ giữa các hành động

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_control);

        statusTextView = findViewById(R.id.statusTextView);
        currentVolumeTextView = findViewById(R.id.currentVolumeTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Cảm biến gia tốc không có sẵn trên thiết bị này!", Toast.LENGTH_SHORT).show();
            statusTextView.setText("Không có cảm biến gia tốc");
            finish();
        } else {
            statusTextView.setText("Sẵn sàng điều chỉnh âm lượng");
            updateVolumeDisplay();
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
    }

    /**
     * Xử lý khi có thay đổi từ cảm biến
     * Phát hiện khi thiết bị nghiêng để tăng/giảm âm lượng
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Xử lý dữ liệu từ cảm biến gia tốc
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Gia tốc trục X (nghiêng trái/phải)

            // Tránh điều chỉnh quá nhanh bằng cách kiểm tra thời gian
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActionTime < ACTION_DELAY) {
                return; // Tránh điều chỉnh quá nhanh
            }

            // Nghiêng trái (x dương) để tăng âm lượng
            if (x > INCREASE_TILT_THRESHOLD) {
                lastActionTime = currentTime;
                adjustVolume(true); // Tăng âm lượng
                statusTextView.setText("Đã tăng âm lượng");
            }
            // Nghiêng phải (x âm) để giảm âm lượng
            else if (x < DECREASE_TILT_THRESHOLD) {
                lastActionTime = currentTime;
                adjustVolume(false); // Giảm âm lượng
                statusTextView.setText("Đã giảm âm lượng");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void adjustVolume(boolean increase) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (increase) {
            // Tăng âm lượng
            if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, AudioManager.FLAG_SHOW_UI);
            }
        } else {
            // Giảm âm lượng
            if (currentVolume > 0) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, AudioManager.FLAG_SHOW_UI);
            }
        }
        
        // Cập nhật hiển thị âm lượng
        updateVolumeDisplay();
    }
    
    private void updateVolumeDisplay() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolumeTextView.setText("Âm lượng hiện tại: " + currentVolume + "/" + maxVolume);
    }
}
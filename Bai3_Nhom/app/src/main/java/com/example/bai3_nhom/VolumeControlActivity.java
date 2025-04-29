package com.example.bai3_nhom;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VolumeControlActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AudioManager audioManager;

    private static final float INCREASE_TILT_THRESHOLD = 5.0f; // Ngưỡng nghiêng trái để tăng âm lượng
    private static final float DECREASE_TILT_THRESHOLD = 10.0f; // Ngưỡng nghiêng phải sâu hơn để giảm âm lượng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Cảm biến gia tốc không có sẵn trên thiết bị này!", Toast.LENGTH_SHORT).show();
            finish();
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Gia tốc trục X (nghiêng trái/phải)

            // Nghiêng trái (x dương) để tăng âm lượng
            if (x > INCREASE_TILT_THRESHOLD) {
                adjustVolume(true);
            }
            // Nghiêng phải sâu hơn (x âm, ngưỡng lớn hơn) để giảm âm lượng
            else if (x < -DECREASE_TILT_THRESHOLD) {
                adjustVolume(false);
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
    }
}
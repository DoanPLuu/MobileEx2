package com.example.bai3_nhom;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SongControlActivity extends AppCompatActivity implements SensorEventListener {

    // Quản lý cảm biến thiết bị
    private SensorManager sensorManager;
    // Cảm biến gia tốc để phát hiện chuyển động nghiêng
    private Sensor accelerometer;
    // Quản lý âm thanh của thiết bị
    private AudioManager audioManager;
    // Hiển thị trạng thái hiện tại
    private TextView statusTextView;
    
    // Ngưỡng nghiêng để kích hoạt chuyển bài
    private static final float TILT_THRESHOLD = 3.0f;
    // Thời gian của hành động cuối cùng để tránh kích hoạt liên tục
    private long lastActionTime = 0;
    // Độ trễ giữa các hành động (1 giây)
    private static final long ACTION_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_control);

        statusTextView = findViewById(R.id.statusTextView);
        
        // Khởi tạo AudioManager để điều khiển media
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        // Khởi tạo cảm biến
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Cảm biến gia tốc không có sẵn!", Toast.LENGTH_SHORT).show();
            statusTextView.setText("Không có cảm biến gia tốc");
            finish();
        } else {
            statusTextView.setText("Nghiêng thiết bị sang trái/phải để điều khiển nhạc");
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
     * Phát hiện khi thiết bị nghiêng để chuyển bài hát
     */
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
                sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_NEXT);
                statusTextView.setText("Đã chuyển bài tiếp theo");
            }
            // Xoay phải (X âm) để quay lại bài trước
            else if (x < -TILT_THRESHOLD) {
                lastActionTime = currentTime;
                sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                statusTextView.setText("Đã quay lại bài trước");
            }
        }
    }

    /**
     * Xử lý khi độ chính xác của cảm biến thay đổi (không sử dụng)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Gửi lệnh điều khiển media thông qua sự kiện phím ảo
     * @param keyCode Mã phím (next/previous) để điều khiển media
     */
    private void sendMediaButtonClick(int keyCode) {
        // Gửi lệnh điều khiển media thông qua AudioManager
        try {
            // Gửi sự kiện nhấn nút
            KeyEvent keyDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            audioManager.dispatchMediaKeyEvent(keyDown);
            
            // Gửi sự kiện thả nút
            KeyEvent keyUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
            audioManager.dispatchMediaKeyEvent(keyUp);
            
            Toast.makeText(this, "Đã gửi lệnh: " + 
                    (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT ? "Bài tiếp theo" : 
                     keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS ? "Bài trước" : 
                     "Khác"), 
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi gửi lệnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
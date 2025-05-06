package com.example.bai3_nhom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothControlActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor; // Thêm cảm biến gia tốc để phát hiện lắc
    private AudioManager audioManager;
    private TextView statusTextView;

    private static final float ROTATION_THRESHOLD = 1.5f; // Ngưỡng xoay để kích hoạt
    private static final float SHAKE_THRESHOLD = 10.0f; // Giảm từ 15.0f xuống 10.0f
    private static final int BLUETOOTH_PERMISSION_REQUEST = 200;
    private long lastActionTime = 0;
    private static final long ACTION_DELAY = 1000; // 1 giây chờ giữa các hành động

    // Biến để phát hiện lắc
    private float lastAcceleration = 0;
    private float currentAcceleration = 0;
    private float acceleration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_control);

        statusTextView = findViewById(R.id.statusTextView);
        
        // Khởi tạo AudioManager để điều khiển media
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        // Khởi tạo SensorManager và cảm biến
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (gyroscopeSensor == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ cảm biến con quay hồi chuyển", Toast.LENGTH_SHORT).show();
            statusTextView.setText("Không có cảm biến gyroscope");
            finish();
            return;
        }

        if (accelerometerSensor == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ cảm biến gia tốc", Toast.LENGTH_SHORT).show();
        }

        // Khởi tạo giá trị gia tốc
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        // Kiểm tra quyền Bluetooth
        checkBluetoothPermissions();
    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 trở lên cần quyền BLUETOOTH_CONNECT
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST);
            } else {
                checkBluetoothStatus();
            }
        } else {
            // Phiên bản Android cũ hơn
            checkBluetoothStatus();
        }
    }

    private void checkBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Thiết bị không hỗ trợ Bluetooth
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show();
            statusTextView.setText("Không hỗ trợ Bluetooth");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth chưa được bật
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) 
                    == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(enableBtIntent, 1);
            } else {
                Toast.makeText(this, "Cần quyền Bluetooth để tiếp tục", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Bluetooth đã bật
            statusTextView.setText("Bluetooth đã sẵn sàng\nXoay thiết bị để điều khiển tai nghe");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            handleGyroscopeEvent(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerEvent(event);
        }
    }

    private void handleGyroscopeEvent(SensorEvent event) {
        float rotationZ = event.values[2]; // Xoay quanh trục Z (xoay thiết bị như vô lăng)
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < ACTION_DELAY) {
            return; // Tránh kích hoạt liên tục
        }

        // Xoay theo chiều kim đồng hồ (Z âm) - Chuyển bài tiếp theo
        if (rotationZ < -ROTATION_THRESHOLD) {
            lastActionTime = currentTime;
            sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_NEXT);
            statusTextView.setText("Đã chuyển bài tiếp theo");
        } 
        // Xoay ngược chiều kim đồng hồ (Z dương) - Quay lại bài trước
        else if (rotationZ > ROTATION_THRESHOLD) {
            lastActionTime = currentTime;
            sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            statusTextView.setText("Đã quay lại bài trước");
        }
    }

    private void handleAccelerometerEvent(SensorEvent event) {
        // Tính toán gia tốc để phát hiện lắc
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;
        
        // Thêm log để debug
        if (Math.abs(acceleration) > 5.0f) {
            // Log giá trị gia tốc để kiểm tra
            System.out.println("Acceleration: " + acceleration);
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < ACTION_DELAY) {
            return; // Tránh kích hoạt liên tục
        }
        
        // Nếu lắc đủ mạnh, phát/tạm dừng
        if (Math.abs(acceleration) > SHAKE_THRESHOLD) {
            lastActionTime = currentTime;
            sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            statusTextView.setText("Đã phát/tạm dừng");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }

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
                     keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ? "Phát/Tạm dừng" : "Khác"), 
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi gửi lệnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetoothStatus();
            } else {
                Toast.makeText(this, "Cần quyền Bluetooth để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                statusTextView.setText("Bluetooth đã sẵn sàng\nXoay thiết bị để điều khiển tai nghe");
            } else {
                statusTextView.setText("Bluetooth chưa được bật");
                Toast.makeText(this, "Cần bật Bluetooth để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
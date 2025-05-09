package com.example.bai3_nhom;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class LightControlActivity extends AppCompatActivity implements SensorEventListener {

    // Quản lý cảm biến thiết bị
    private SensorManager sensorManager;
    // Cảm biến tiệm cận để phát hiện khi có vật thể gần thiết bị
    private Sensor proximitySensor;
    // Trạng thái đèn (bật/tắt)
    private boolean isLightOn = false;

    // UI elements
    private ImageView lightBulbImage;
    private ConstraintLayout rootLayout;

    // Quản lý camera để điều khiển đèn flash
    private CameraManager cameraManager;
    private String cameraId;

    // Mã yêu cầu quyền camera
    private static final int CAMERA_REQUEST_CODE = 100;

    /**
     * Khởi tạo activity và các thành phần cần thiết
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lightonof);

        lightBulbImage = findViewById(R.id.lightBulb);
        rootLayout = findViewById(R.id.rootLayout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (proximitySensor == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ cảm biến tiệm cận", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Khởi tạo camera manager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Kiểm tra và yêu cầu quyền camera
        checkAndRequestCameraPermission();

        TextView instructionTextView = findViewById(R.id.instructionTextView);
        instructionTextView.setText("Đưa tay gần cảm biến để bật/tắt đèn");
    }

    /**
     * Kiểm tra và yêu cầu quyền truy cập camera nếu chưa được cấp
     * Cần quyền này để điều khiển đèn flash
     */
    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            
            // Kiểm tra xem có nên hiển thị giải thích tại sao cần quyền không
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Hiển thị lý do tại sao ứng dụng cần quyền này
                Toast.makeText(this, 
                    "Ứng dụng cần quyền camera để điều khiển đèn flash. Vui lòng cấp quyền.", 
                    Toast.LENGTH_LONG).show();
            }
            
            // Yêu cầu quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        } else {
            // Đã có quyền, tiếp tục
            Toast.makeText(this, "Đã có quyền camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kiểm tra xem thiết bị có hỗ trợ đèn flash không
     * Nếu không hỗ trợ, thông báo và đóng activity
     */
    private void checkFlashAvailability() {
        boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) {
            Toast.makeText(this, "Thiết bị không hỗ trợ đèn flash", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Đèn flash sẵn sàng sử dụng", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Đăng ký lắng nghe sự kiện cảm biến khi activity được hiển thị
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Hủy đăng ký lắng nghe sự kiện cảm biến khi activity bị tạm dừng
     * Giúp tiết kiệm pin và tài nguyên
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Xử lý khi có thay đổi từ cảm biến
     * Phát hiện khi có vật thể gần cảm biến tiệm cận để bật/tắt đèn
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            float maxRange = proximitySensor.getMaximumRange();
            
            if (distance < maxRange) {
                toggleLight();
            }
        }
    }

    /**
     * Xử lý khi độ chính xác của cảm biến thay đổi (không sử dụng)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Chuyển đổi trạng thái đèn (bật/tắt)
     * Cập nhật giao diện và điều khiển đèn flash
     */
    private void toggleLight() {
        isLightOn = !isLightOn;
        if (isLightOn) {
            lightBulbImage.setImageResource(R.drawable.ic_light_on);
            rootLayout.setBackgroundColor(Color.YELLOW);
            turnOnFlash(true);
        } else {
            lightBulbImage.setImageResource(R.drawable.ic_light_off);
            rootLayout.setBackgroundColor(Color.BLACK);
            turnOnFlash(false);
        }
    }

    /**
     * Điều khiển đèn flash của camera
     * @param status true để bật, false để tắt
     */
    private void turnOnFlash(boolean status) {
        if (cameraManager != null && cameraId != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(cameraId, status);
                    
                    // Xóa Toast debug này
                    // Toast.makeText(this, "Đèn flash: " + (status ? "BẬT" : "TẮT"), Toast.LENGTH_SHORT).show();
                }
            } catch (CameraAccessException e) {
                Toast.makeText(this, "Lỗi truy cập đèn flash: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Không thể truy cập camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý kết quả sau khi yêu cầu quyền camera
     * Hiển thị thông báo phù hợp dựa trên quyết định của người dùng
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã được cấp quyền camera", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không có quyền CAMERA, không thể bật flash!", 
                        Toast.LENGTH_SHORT).show();
                
                // Nếu người dùng từ chối và chọn "Không hỏi lại", hướng dẫn họ vào cài đặt
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, 
                        "Vui lòng cấp quyền camera trong Cài đặt > Ứng dụng > Quyền", 
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

package com.example.bai3_nhom;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FlashBeatActivity extends AppCompatActivity {

    private static final String TAG = "FlashBeatActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    
    private TextView statusTextView;
    private TextView sensitivityValueTextView;
    private Button toggleButton;
    private SeekBar sensitivitySeekBar;
    
    private int threshold = 3000; // Ngưỡng âm thanh mặc định
    private Handler handler = new Handler();
    private Runnable flashOffRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_beat);
        
        statusTextView = findViewById(R.id.statusTextView);
        sensitivityValueTextView = findViewById(R.id.sensitivityValueTextView);
        toggleButton = findViewById(R.id.toggleButton);
        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);
        
        // Khởi tạo camera manager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Không thể truy cập camera: " + e.getMessage());
        }
        
        // Khởi tạo Runnable để tắt đèn flash sau một khoảng thời gian
        flashOffRunnable = () -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(cameraId, false);
                    isFlashOn = false;
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, "Lỗi khi tắt đèn flash: " + e.getMessage());
            }
        };
        
        // Thiết lập SeekBar để điều chỉnh độ nhạy
        sensitivitySeekBar.setMax(10000);
        sensitivitySeekBar.setProgress(threshold);
        sensitivityValueTextView.setText("Độ nhạy: " + threshold);
        
        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress;
                sensitivityValueTextView.setText("Độ nhạy: " + threshold);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Thiết lập nút bật/tắt
        toggleButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
                toggleButton.setText("Bắt đầu");
                statusTextView.setText("Đã dừng");
            } else {
                if (checkPermissions()) {
                    startRecording();
                    toggleButton.setText("Dừng");
                    statusTextView.setText("Đang phân tích âm thanh...");
                }
            }
        });
        
        // Kiểm tra quyền
        checkPermissions();
    }
    
    private boolean checkPermissions() {
        // Kiểm tra quyền ghi âm
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.RECORD_AUDIO}, 
                    REQUEST_RECORD_AUDIO_PERMISSION);
            return false;
        }
        
        // Kiểm tra quyền camera (để sử dụng đèn flash)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    REQUEST_CAMERA_PERMISSION);
            return false;
        }
        
        return true;
    }
    
    private void startRecording() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, 
                AUDIO_FORMAT, BUFFER_SIZE);
        
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Toast.makeText(this, "Không thể khởi tạo AudioRecord", Toast.LENGTH_SHORT).show();
            return;
        }
        
        audioRecord.startRecording();
        isRecording = true;
        
        recordingThread = new Thread(() -> {
            short[] buffer = new short[BUFFER_SIZE];
            while (isRecording) {
                int readSize = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (readSize > 0) {
                    analyzeAudio(buffer, readSize);
                }
            }
        });
        
        recordingThread.start();
    }
    
    private void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }
        
        // Đảm bảo tắt đèn flash
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Lỗi khi tắt đèn flash: " + e.getMessage());
        }
    }
    
    private void analyzeAudio(short[] buffer, int readSize) {
        // Tính toán biên độ âm thanh
        int sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += Math.abs(buffer[i]);
        }
        int average = sum / readSize;
        
        // Log biên độ để debug
        Log.d(TAG, "Biên độ âm thanh: " + average);
        
        // Nếu biên độ vượt ngưỡng, bật đèn flash
        if (average > threshold) {
            flashBeat();
        }
    }
    
    private void flashBeat() {
        // Nếu đèn đã bật, không làm gì
        if (isFlashOn) {
            return;
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Bật đèn flash
                cameraManager.setTorchMode(cameraId, true);
                isFlashOn = true;
                
                // Hủy bỏ Runnable cũ nếu có
                handler.removeCallbacks(flashOffRunnable);
                
                // Đặt lịch tắt đèn flash sau 100ms
                handler.postDelayed(flashOffRunnable, 100);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Lỗi khi bật đèn flash: " + e.getMessage());
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION || requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp
                Toast.makeText(this, "Quyền đã được cấp", Toast.LENGTH_SHORT).show();
            } else {
                // Quyền bị từ chối
                Toast.makeText(this, "Cần quyền để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording();
            toggleButton.setText("Bắt đầu");
            statusTextView.setText("Đã dừng");
        }
    }
}
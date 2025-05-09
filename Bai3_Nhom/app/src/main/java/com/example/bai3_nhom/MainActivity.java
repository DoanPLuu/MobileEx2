package com.example.bai3_nhom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button lightControlButton;
    private Button volumeControlButton;
    private Button songControlButton;
    private Button iotControlButton;
    private Button flashBeatButton;

    /**
     * Khởi tạo activity chính và thiết lập các nút điều hướng
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các nút điều khiển
        lightControlButton = findViewById(R.id.lightControlButton);
        volumeControlButton = findViewById(R.id.volumeControlButton);
        songControlButton = findViewById(R.id.songControlButton);
        iotControlButton = findViewById(R.id.iotControlButton);
        flashBeatButton = findViewById(R.id.flashBeatButton);

        // Thiết lập sự kiện click cho nút điều khiển đèn
        lightControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LightControlActivity.class);
            startActivity(intent); // Chuyển đến màn hình điều khiển đèn
        });

        volumeControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VolumeControlActivity.class);
            startActivity(intent);
        });

        songControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SongControlActivity.class);
            startActivity(intent);
        });

        iotControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BluetoothControlActivity.class);
            startActivity(intent);
        });
        
        flashBeatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FlashBeatActivity.class);
            startActivity(intent);
        });
    }
}

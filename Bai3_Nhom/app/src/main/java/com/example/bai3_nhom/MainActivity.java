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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các nút
        lightControlButton = findViewById(R.id.lightControlButton);
        volumeControlButton = findViewById(R.id.volumeControlButton);
        songControlButton = findViewById(R.id.songControlButton);
        iotControlButton = findViewById(R.id.iotControlButton);

        // Thêm sự kiện cho các nút
        lightControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LightControlActivity.class);
            startActivity(intent);
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
    }
}

package com.example.localmessenger;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity; // ✅ Đúng thư viện

public class MainActivity extends AppCompatActivity { // ✅ Phải là AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

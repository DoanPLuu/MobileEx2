package com.example.countthread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private ProgressBar progressBar;
    private Button startButton, stopButton;
    private Thread countingThread;
    private boolean isRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> startCounting());
        stopButton.setOnClickListener(v -> stopCounting());
    }

    private void startCounting() {
        if (isRunning) return;
        isRunning = true;
        textView.setText("0");
        progressBar.setProgress(0);
        countingThread = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                if (!isRunning) break;
                final int count = i;
                handler.post(() -> {
                    textView.setText(String.valueOf(count));
                    progressBar.setProgress(count);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (isRunning) {
                handler.post(() -> textView.setText("Hoàn thành!"));
            }
            isRunning = false;
        });
        countingThread.start();
    }

    private void stopCounting() {
        isRunning = false;
        if (countingThread != null) {
            countingThread.interrupt();
        }
    }
}
package com.example.threadcounter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button startButton, stopButton;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Thread countingThread;
    private boolean isCounting = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        progressBar = findViewById(R.id.progressBar);
        stopButton.setVisibility(View.GONE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCounting();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCounting();
            }
        });
    }

    private void startCounting() {
        if (isCounting) return;

        isCounting = true;
        startButton.setEnabled(false);
        stopButton.setVisibility(View.VISIBLE); // Hiển thị nút Stop khi bắt đầu đếm
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0); // Đặt lại progress về 0


        countingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 10; i++) {
                    if (!isCounting) return; // Thoát hẳn khỏi hàm nếu dừng

                    final int count = i;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(String.valueOf(count));
                            progressBar.setProgress(count);
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                // Nếu không bị dừng, hiển thị "Hoàn thành!"
                if (isCounting) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("Hoàn thành!");
                            startButton.setEnabled(true);
                            stopButton.setVisibility(View.GONE);
                        }
                    });
                }

                isCounting = false;
            }
        });

        countingThread.start();
    }

    private void stopCounting() {
        if (!isCounting) return;

        isCounting = false;
        if (countingThread != null) {
            countingThread.interrupt();
        }

        // Hiển thị "Đã dừng!" ngay lập tức
        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText("Đã dừng!");
                startButton.setEnabled(true);
                stopButton.setVisibility(View.GONE);
            }
        });
    }
}

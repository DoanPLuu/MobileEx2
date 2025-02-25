package com.example.internetspeedtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnStartTest;
    private TextView tvResult;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần UI
        btnStartTest = findViewById(R.id.btn_start_test);
        tvResult = findViewById(R.id.tv_result);
        progressBar = findViewById(R.id.progress_bar);

        // Thiết lập sự kiện click cho nút bắt đầu kiểm tra
        btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra quyền INTERNET (thường được cấp mặc định, nhưng nên kiểm tra)
                if (hasInternetPermission()) {
                    // Bắt đầu kiểm tra tốc độ
                    new NetworkSpeedTest().execute();
                } else {
                    // Yêu cầu quyền Internet nếu chưa có
                    requestInternetPermission();
                }
            }
        });
    }

    // Kiểm tra quyền Internet
    private boolean hasInternetPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Yêu cầu quyền Internet
    private void requestInternetPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET}, 1);
    }

    // AsyncTask để thực hiện kiểm tra tốc độ mạng
    private class NetworkSpeedTest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            // Hiển thị ProgressBar và vô hiệu hóa nút bắt đầu
            progressBar.setVisibility(View.VISIBLE);
            btnStartTest.setEnabled(false);
            tvResult.setText("Đang kiểm tra tốc độ mạng...");
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            String resultMessage;

            try {
                // Bước 1: Đo thời gian phản hồi
                long startTime = System.currentTimeMillis();
                Request request = new Request.Builder()
                        .url("https://www.google.com")
                        .build();

                Response response = client.newCall(request).execute();
                long endTime = System.currentTimeMillis();

                // Tính thời gian phản hồi
                long responseTime = endTime - startTime;

                // Đánh giá tốc độ mạng
                String speedRating;
                if (responseTime < 300) {
                    speedRating = "Rất nhanh";
                } else if (responseTime < 600) {
                    speedRating = "Nhanh";
                } else if (responseTime < 1000) {
                    speedRating = "Trung bình";
                } else {
                    speedRating = "Chậm";
                }

                // Lấy mã trạng thái HTTP
                int statusCode = response.code();

                // Tạo thông báo kết quả
                resultMessage = "Kết quả kiểm tra:\n" +
                        "- Thời gian phản hồi: " + responseTime + " ms\n" +
                        "- Đánh giá tốc độ: " + speedRating + "\n" +
                        "- Mã trạng thái HTTP: " + statusCode;

                // Đóng response để tránh rò rỉ tài nguyên
                response.close();

            } catch (IOException e) {
                // Xử lý ngoại lệ khi không thể kết nối
                resultMessage = "Lỗi kết nối: " + e.getMessage();
            }

            return resultMessage;
        }

        @Override
        protected void onPostExecute(String result) {
            // Hiển thị kết quả và khôi phục trạng thái UI
            tvResult.setText(result);
            progressBar.setVisibility(View.GONE);
            btnStartTest.setEnabled(true);
        }
    }
}
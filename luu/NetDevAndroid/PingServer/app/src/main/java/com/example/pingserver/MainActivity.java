package com.example.pingserver;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPing = findViewById(R.id.btnPing);
        tvResult = findViewById(R.id.tvResult);

        btnPing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PingTask().execute("8.8.8.8");
            }
        });
    }

    // AsyncTask để chạy thao tác ping trong nền
    private class PingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String host = params[0];
            StringBuilder result = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec("ping -c 4 " + host);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (Exception e) {
                result.append("Lỗi khi ping: ").append(e.getMessage());
                Log.e("PingTask", "Lỗi khi ping", e);
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            tvResult.setText(result);
        }
    }
}

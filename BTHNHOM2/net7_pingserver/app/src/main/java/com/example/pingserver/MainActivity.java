package com.example.pingserver;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private Button pingButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pingButton = findViewById(R.id.pingButton);
        resultTextView = findViewById(R.id.resultTextView);

        pingButton.setOnClickListener(v -> new PingTask().execute("google.com"));
    }

    private class PingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Process process = Runtime.getRuntime().exec("ping -c 1 " + params[0]);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                    if (line.contains("time=")) {
                        return line.substring(line.indexOf("time=") + 5);
                    }
                }
                return "No ping result found\n" + result.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            resultTextView.setText("Result: " + result);
        }
    }
}
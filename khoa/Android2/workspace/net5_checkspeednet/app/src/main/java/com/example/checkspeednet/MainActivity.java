package com.example.checkspeednet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button checkButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkButton = findViewById(R.id.checkButton);
        resultTextView = findViewById(R.id.resultTextView);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckNetworkSpeed().execute();
            }
        });
    }

    private class CheckNetworkSpeed extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                long startTime = System.currentTimeMillis();
                Request request = new Request.Builder().url("https://www.google.com").build();
                Response response = client.newCall(request).execute();
                long endTime = System.currentTimeMillis();
                long speed = endTime - startTime;
                return "Response time: " + speed + " ms";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            resultTextView.setText(result);
        }
    }
}
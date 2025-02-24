package com.example.internetchecker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        if (isNetworkAvailable()) {
            new CheckInternetTask().execute("https://www.google.com");
        } else {

            statusText.setText("Không có kết nối mạng");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork =  cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private class CheckInternetTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground (String... urls) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
                urlConnection.setRequestMethod("HEAD");
                urlConnection.setConnectTimeout(3000);
                urlConnection.setReadTimeout(3000);
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                return (responseCode == 200);
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute (Boolean result) {
            if (result) {
                statusText.setText("Kết nối thành công đến Google");
            } else {
                statusText.setText("Không thể kết nối đến Google");
            }
        }
    }
}
package com.example.getapiusers;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        new GetUsersTask().execute("https://jsonplaceholder.typicode.com/users");
    }

    private  class GetUsersTask extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                reader.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                return "Loi: " + e.getMessage();
            }
            return result.toString();
        }
        @Override
        protected  void onPostExecute(String s) {textView.setText(s);}
    }

}
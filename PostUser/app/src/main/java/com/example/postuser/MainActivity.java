package com.example.postuser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText;
    private TextView postResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        postResultTextView = findViewById(R.id.postResultTextView);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            String userName = nameEditText.getText().toString().trim();
            String userEmail = emailEditText.getText().toString().trim();

            if (!userName.isEmpty() && !userEmail.isEmpty()) {
                new PostUserTask().execute(userName, userEmail);
            } else {
                postResultTextView.setText("Vui lòng nhập đầy đủ thông tin");
            }
        });
    }

    private class PostUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String userName = params[0];
            String userEmail = params[1];
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL("https://jsonplaceholder.typicode.com/users");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                JSONObject postData = new JSONObject();
                postData.put("name", userName);
                postData.put("email", userEmail);

                OutputStream os = connection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                writer.write(postData.toString());
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                return "Lỗi: " + e.getMessage();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            postResultTextView.setText(result);
        }
    }
}


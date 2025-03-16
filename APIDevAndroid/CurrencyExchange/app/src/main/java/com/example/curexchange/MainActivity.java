package com.example.curexchange;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCurrency;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCurrency = findViewById(R.id.editTextCurrency);
        textViewResult = findViewById(R.id.textViewResult);
        Button btnConvert = findViewById(R.id.btnConvert);

        btnConvert.setOnClickListener(v -> {
            String currency = editTextCurrency.getText().toString().trim().toUpperCase();
            if (!currency.isEmpty()) {
                new FetchExchangeRateTask().execute(currency);
            } else {
                Toast.makeText(this, "Vui lòng nhập mã tiền tệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class FetchExchangeRateTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String currency = params[0];
            String apiUrl = "https://api.frankfurter.app/latest?from=USD&to=" + currency;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                return result.toString();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject rates = jsonObject.getJSONObject("rates");

                    String currency = editTextCurrency.getText().toString().toUpperCase();
                    if (rates.has(currency)) {
                        double exchangeRate = rates.getDouble(currency);
                        textViewResult.setText("1 USD = " + exchangeRate + " " + currency);
                    } else {
                        textViewResult.setText("Mã tiền tệ không hợp lệ.");
                    }

                } catch (Exception e) {
                    textViewResult.setText("Lỗi khi xử lý dữ liệu.");
                }
            } else {
                textViewResult.setText("Lỗi kết nối hoặc mã tiền tệ không hợp lệ.");
            }
        }
    }
}

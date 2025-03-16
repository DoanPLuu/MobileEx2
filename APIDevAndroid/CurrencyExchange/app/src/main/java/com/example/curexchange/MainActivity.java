package com.example.curexchange;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCurrency;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        textViewResult = findViewById(R.id.textViewResult);
        Button btnConvert = findViewById(R.id.btnConvert);

        // Thiết lập danh sách tiền tệ cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.currency_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        btnConvert.setOnClickListener(v -> {
            String currency = spinnerCurrency.getSelectedItem().toString();
            new FetchExchangeRateTask().execute(currency);
        });
    }

    private class FetchExchangeRateTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String currency = params[0];
            String apiKey = "1315b589c70ea7fae87fb6b6";
            String apiUrl = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD";

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
                    JSONObject rates = jsonObject.getJSONObject("conversion_rates");

                    String currency = spinnerCurrency.getSelectedItem().toString();
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

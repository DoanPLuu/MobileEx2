package com.example.bai1_nhom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bai1_nhom.ApiService;
import com.example.bai1_nhom.ConversionHistory;
import com.example.bai1_nhom.ExchangeRateResponse;
import com.example.bai1_nhom.HistoryActivity;
import com.example.bai1_nhom.HistoryStorage;
import com.example.bai1_nhom.R;
import com.example.bai1_nhom.TimeSeriesResponse;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// ... (các import giữ nguyên như cũ)

public class MainActivity extends AppCompatActivity {
    private Spinner spinnerFrom, spinnerTo;
    private EditText editAmount;
    private TextView textResult;
    private Button btnConvert, btnHistory;
    private LineChart lineChart;
    private ApiService apiService;
    private static final String API_KEY = "cca74838d192d558cc0c142f";
    private Map<String, Double> exchangeRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        editAmount = findViewById(R.id.edit_amount);
        textResult = findViewById(R.id.text_result);
        btnConvert = findViewById(R.id.btn_convert);
        btnHistory = findViewById(R.id.btn_history);
        lineChart = findViewById(R.id.line_chart);

        String[] currencies = {"USD", "VND", "EUR", "JPY"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://v6.exchangerate-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        fetchExchangeRates("USD");

        btnConvert.setOnClickListener(v -> {
            String fromCurrency = spinnerFrom.getSelectedItem().toString();
            fetchExchangeRates(fromCurrency); // Cập nhật tỷ giá từ đơn vị được chọn
            new Handler().postDelayed(() -> convertCurrency(), 1000); // đợi 1s để có response
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void fetchExchangeRates(String baseCurrency) {
        Call<ExchangeRateResponse> call = apiService.getLatestRates(API_KEY, baseCurrency);
        call.enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exchangeRates = response.body().getConversionRates();
                    Toast.makeText(MainActivity.this, "Rates loaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load rates", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertCurrency() {
        String fromCurrency = spinnerFrom.getSelectedItem().toString();
        String toCurrency = spinnerTo.getSelectedItem().toString();
        String amountStr = editAmount.getText().toString();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exchangeRates == null || !exchangeRates.containsKey(toCurrency)) {
            Toast.makeText(this, "Rates not available. Trying to reload...", Toast.LENGTH_SHORT).show();
            fetchExchangeRates(fromCurrency);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }

        double result = fromCurrency.equals(toCurrency)
                ? amount
                : amount * exchangeRates.get(toCurrency);

        textResult.setText(String.format(Locale.getDefault(), "%.2f %s", result, toCurrency));

        ConversionHistory history = new ConversionHistory(
                amount, fromCurrency, result, toCurrency,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
        );
        HistoryStorage.getInstance(this).addHistory(history);

        updateChart(fromCurrency, toCurrency);
    }

    private void updateChart(String fromCurrency, String toCurrency) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String endDate = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String startDate = sdf.format(calendar.getTime());

        Call<TimeSeriesResponse> call = apiService.getTimeSeries(API_KEY, fromCurrency, startDate, endDate, toCurrency);
        call.enqueue(new Callback<TimeSeriesResponse>() {
            @Override
            public void onResponse(Call<TimeSeriesResponse> call, Response<TimeSeriesResponse> response) {
                Toast.makeText(MainActivity.this, String.valueOf(response.isSuccessful()), Toast.LENGTH_SHORT).show();

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Map<String, Double>> rates = response.body().getRates();
                    List<Entry> entries = new ArrayList<>();
                    Toast.makeText(MainActivity.this, "a", Toast.LENGTH_SHORT).show();

                    int index = 0;
                    for (Map.Entry<String, Map<String, Double>> entry : rates.entrySet()) {
                        Double rate = entry.getValue().get(toCurrency);
                        if (rate != null) {
                            entries.add(new Entry(index++, rate.floatValue()));
                        }
                    }
                    LineDataSet dataSet = new LineDataSet(entries, fromCurrency + "/" + toCurrency);
                    dataSet.setColor(getResources().getColor(android.R.color.holo_blue_light));
                    dataSet.setValueTextColor(getResources().getColor(android.R.color.black));
                    lineChart.setData(new LineData(dataSet));
                    lineChart.invalidate();
                }
            }

            @Override
            public void onFailure(Call<TimeSeriesResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Chart load failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

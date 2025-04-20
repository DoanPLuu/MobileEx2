package com.example.getapination;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private Button btnFetch;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> countryNames;
    private ExecutorService executorService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        btnFetch = findViewById(R.id.btnFetch);
        countryNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, countryNames);
        listView.setAdapter(adapter);

        // Tao ExecutorService va Handler
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCountries();
            }
        });
    }
    private void fetchCountries() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Country> countries = NetworkUtils.fetchCountries();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (countries.isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                    "Loi lay du lieu!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            updateUI(countries);
                        }
                    }
                });
            }
        });
    }
    private void updateUI(ArrayList<Country> countries) {
        countryNames.clear();
        for (Country country : countries) {
            countryNames.add(country.toString());
        }
        adapter.notifyDataSetChanged();
    }
}
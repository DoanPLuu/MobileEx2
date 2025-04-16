package com.example.bai1_nhom;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView listView = findViewById(R.id.list_history);
        List<ConversionHistory> historyList = HistoryStorage.getInstance(this).getHistory();

        ArrayAdapter<ConversionHistory> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);
    }
}
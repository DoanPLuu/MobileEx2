package com.example.restcountry;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnFetchCountries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listViewCountries);
        btnFetchCountries = findViewById(R.id.btnFetchCountries);

        btnFetchCountries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchCountriesTask().execute();
            }
        });
    }

    private class FetchCountriesTask extends AsyncTask<Void, Void, ArrayList<Country>> {

        @Override
        protected ArrayList<Country> doInBackground(Void... voids) {
            return NetworkUtils.fetchCountries();
        }

        @Override
        protected void onPostExecute(ArrayList<Country> countries) {
            if (countries != null && !countries.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        formatCountryList(countries));
                listView.setAdapter(adapter);
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ArrayList<String> formatCountryList(ArrayList<Country> countries) {
        ArrayList<String> formattedList = new ArrayList<>();
        for (Country country : countries) {
            formattedList.add(country.getName() + " - " + country.getCapital() + " - " + country.getRegion());
        }
        return formattedList;
    }
}
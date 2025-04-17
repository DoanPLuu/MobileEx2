package com.example.bai3;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new FetchNewsTask().execute("https://vnexpress.net/rss/tin-moi-nhat.rss");
    }

    private class FetchNewsTask extends AsyncTask<String, Void, List<NewsItem>> {
        @Override
        protected List<NewsItem> doInBackground(String... urls) {
            return RssParser.getRSSFeed(urls[0]);
        }

        @Override
        protected void onPostExecute(List<NewsItem> newsItems) {
            Log.d("RSS_DEBUG", "Số bài viết lấy được: " + newsItems.size());
            for (NewsItem item : newsItems) {
                Log.d("RSS_ITEM", item.getTitle() + " - " + item.getLink());
            }

            adapter = new NewsAdapter(MainActivity.this, newsItems);
            recyclerView.setAdapter(adapter);
        }
    }
}

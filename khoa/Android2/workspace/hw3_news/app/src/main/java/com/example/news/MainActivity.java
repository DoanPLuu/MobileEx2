package com.example.news;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView rssListView;
    private ArrayAdapter<String> adapter;
    private List<RSSItem> rssItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssListView = findViewById(R.id.rssListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        rssListView.setAdapter(adapter);

        new LoadRSSFeed().execute("https://vnexpress.net/rss/tin-moi-nhat.rss");

        rssListView.setOnItemClickListener((parent, view, position, id) -> {
            String url = rssItems.get(position).getLink();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    private class LoadRSSFeed extends AsyncTask<String, Void, List<RSSItem>> {
        @Override
        protected List<RSSItem> doInBackground(String... urls) {
            return RSSParser.getRSSFeed(urls[0]);
        }

        @Override
        protected void onPostExecute(List<RSSItem> items) {
            rssItems.clear();
            rssItems.addAll(items);
            List<String> titles = new ArrayList<>();
            for (RSSItem item : rssItems) {
                titles.add(item.getTitle());
            }
            adapter.clear();
            adapter.addAll(titles);
            adapter.notifyDataSetChanged();
        }
    }
}

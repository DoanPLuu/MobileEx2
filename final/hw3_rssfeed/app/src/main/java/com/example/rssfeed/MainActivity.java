package com.example.rssfeed;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RssFeedFetcher.RssFeedListener {
    private ListView listView;
    private ProgressBar progressBar;
    private NewsAdapter adapter;
    private static final String RSS_FEED_URL = "https://baotintuc.vn/tin-moi-nhat.rss"; // Hoặc URL RSS khác

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listNews);
        progressBar = findViewById(R.id.progressBar);

        // Bắt sự kiện click item tin tức để mở trang web
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsItem item = adapter.getItem(position);
                if (item != null && item.getLink() != null) {
                    String link = item.getLink().trim();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(link));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Không thể mở link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        // Tải dữ liệu RSS
        loadRssFeed();
    }

    private void loadRssFeed() {
        progressBar.setVisibility(View.VISIBLE);
        new RssFeedFetcher(this).execute(RSS_FEED_URL);
    }

    @Override
    public void onFetchComplete(List<NewsItem> newsItems) {
        progressBar.setVisibility(View.GONE);
        if (newsItems != null && !newsItems.isEmpty()) {
            adapter = new NewsAdapter(this, newsItems);
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Không có tin tức nào được tìm thấy", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
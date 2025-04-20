package com.example.rssfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class NewsAdapter extends ArrayAdapter<NewsItem> {
    private Context context;
    private List<NewsItem> newsItems;

    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        super(context, R.layout.list_item_news, newsItems);
        this.context = context;
        this.newsItems = newsItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_news, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.tvTitle);
            viewHolder.description = convertView.findViewById(R.id.tvDescription);
            viewHolder.date = convertView.findViewById(R.id.tvDate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        NewsItem newsItem = newsItems.get(position);
        viewHolder.title.setText(newsItem.getTitle());
        viewHolder.description.setText(newsItem.getDescription());
        viewHolder.date.setText(newsItem.getPubDate());

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView description;
        TextView date;
    }
}
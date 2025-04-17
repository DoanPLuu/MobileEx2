package com.example.rssfeed;

import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RssFeedFetcher extends AsyncTask<String, Void, List<NewsItem>> {
    private RssFeedListener listener;

    public interface RssFeedListener {
        void onFetchComplete(List<NewsItem> newsItems);
        void onError(Exception e);
    }

    public RssFeedFetcher(RssFeedListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<NewsItem> doInBackground(String... params) {
        try {
            String feedUrl = params[0];
            URL url = new URL(feedUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            return parseXml(inputStream);
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(e);
            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<NewsItem> newsItems) {
        if (listener != null) {
            if (newsItems != null) {
                // Debug log
                for (NewsItem item : newsItems) {
                    Log.d("RssFeedFetcher", "Title: " + item.getTitle() + ", Link: " + item.getLink());
                }
                listener.onFetchComplete(newsItems);
            } else {
                listener.onError(new Exception("Failed to fetch RSS feed"));
            }
        }
    }

    private List<NewsItem> parseXml(InputStream inputStream) throws XmlPullParserException, IOException {
        List<NewsItem> items = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        String title = null;
        String link = null;
        String description = null;
        String pubDate = null;
        boolean isItem = false;

        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            isItem = true;
                            title = null;
                            link = null;
                            description = null;
                            pubDate = null;
                        } else if (isItem) {
                            if ("title".equalsIgnoreCase(tagName)) {
                                eventType = parser.next();
                                if (eventType == XmlPullParser.TEXT) {
                                    title = parser.getText();
                                }
                            } else if ("link".equalsIgnoreCase(tagName)) {
                                eventType = parser.next();
                                if (eventType == XmlPullParser.TEXT) {
                                    link = parser.getText();
                                }
                            } else if ("description".equalsIgnoreCase(tagName)) {
                                eventType = parser.next();
                                if (eventType == XmlPullParser.TEXT) {
                                    description = parser.getText();
                                }
                            } else if ("pubDate".equalsIgnoreCase(tagName)) {
                                eventType = parser.next();
                                if (eventType == XmlPullParser.TEXT) {
                                    pubDate = parser.getText();
                                }
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            if (title != null && link != null) {
                                // Log để debug
                                Log.d("RssParser", "Adding item: " + title + " - " + link);

                                NewsItem item = new NewsItem(title,
                                        description != null ? description : "",
                                        link,
                                        pubDate != null ? pubDate : "");
                                items.add(item);
                            }
                            isItem = false;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } finally {
            inputStream.close();
        }
        return items;
    }

}
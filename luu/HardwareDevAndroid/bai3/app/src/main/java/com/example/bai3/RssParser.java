package com.example.bai3;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RssParser {

    public static List<NewsItem> getRSSFeed(String urlString) {
        List<NewsItem> newsList = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            String title = "";
            String link = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equalsIgnoreCase("title")) {
                        title = parser.nextText();
                    } else if (parser.getName().equalsIgnoreCase("link")) {
                        link = parser.nextText();
                    }
                }
                if (!title.isEmpty() && !link.isEmpty()) {
                    newsList.add(new NewsItem(title, link));
                    title = "";
                    link = "";
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsList;
    }
}

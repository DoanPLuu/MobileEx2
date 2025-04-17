package com.example.bai3;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
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

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

            int eventType = parser.getEventType();
            String tagName;
            String title = "";
            String link = "";
            boolean insideItem = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (eventType == XmlPullParser.START_TAG) {
                    if ("item".equalsIgnoreCase(tagName)) {
                        insideItem = true;
                    } else if (insideItem) {
                        if ("title".equalsIgnoreCase(tagName)) {
                            title = parser.nextText().trim();
                        } else if ("link".equalsIgnoreCase(tagName)) {
                            link = parser.nextText().trim();
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && "item".equalsIgnoreCase(tagName)) {
                    if (!title.isEmpty() && !link.isEmpty()) {
                        newsList.add(new NewsItem(title, link));
                    }
                    // Reset cho item kế tiếp
                    title = "";
                    link = "";
                    insideItem = false;
                }

                eventType = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }
}

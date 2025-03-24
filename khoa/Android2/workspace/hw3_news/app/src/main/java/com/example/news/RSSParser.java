package com.example.news;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSParser {
    public static List<RSSItem> getRSSFeed(String urlString) {
        List<RSSItem> rssItems = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            String title = null, link = null;
            boolean insideItem = false;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (parser.getName().equalsIgnoreCase("title")) {
                            if (insideItem) title = parser.nextText();
                        } else if (parser.getName().equalsIgnoreCase("link")) {
                            if (insideItem) link = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equalsIgnoreCase("item")) {
                            insideItem = false;
                            if (title != null && link != null) {
                                rssItems.add(new RSSItem(title, link));
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rssItems;
    }
}

package org.asdtm.goodweather;

import android.net.Uri;

import org.asdtm.goodweather.Utils.ApiKeys;
import org.asdtm.goodweather.model.CitySearch;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YahooParser
{
    private static final String TAG = "YahooParser";
    private static String BASE_URL = "http://where.yahooapis.com/v1/";
    private static String APPID = ApiKeys.WHERE_YAHOO_API_KEY;
    private static int COUNT_CITY = 10;

    public static List<CitySearch> getCity(String city, String locale)
    {
        List<CitySearch> resultSearch = new ArrayList<CitySearch>();
        HttpURLConnection whereConnection = null;
        try {
            String query = buildSearchQuery(city, locale);
            URL urlQuery = new URL(query);
            whereConnection = (HttpURLConnection) urlQuery.openConnection();
            InputStream inputStream = whereConnection.getInputStream();

            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
            pullParser.setInput(new InputStreamReader(inputStream));
            int event = pullParser.getEventType();

            CitySearch citySearch = null;
            String tagName = null;
            String currentTag = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = pullParser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        citySearch = new CitySearch();
                    } else if (tagName.equalsIgnoreCase("country")) {
                        citySearch.setCountryCode(pullParser.getAttributeValue(null, "code"));
                    }
                    currentTag = tagName;
                } else if (event == XmlPullParser.TEXT) {
                    if ("name".equals(currentTag)) {
                        assert citySearch != null;
                        citySearch.setCityName(pullParser.getText());
                    } else if (currentTag.equalsIgnoreCase("country")) {
                        assert citySearch != null;
                        citySearch.setCountry(pullParser.getText());
                    } else if (currentTag.equalsIgnoreCase("latitude")) {
                        if (pullParser.getDepth() == 4) {
                            assert citySearch != null;
                            citySearch.setLatitude(pullParser.getText());
                        }
                    }  else if (currentTag.equalsIgnoreCase("longitude")) {
                        if (pullParser.getDepth() == 4) {
                            assert citySearch != null;
                            citySearch.setLongitude(pullParser.getText());
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if ("place".equals(tagName)) {
                        resultSearch.add(citySearch);
                    }
                }

                event = pullParser.next();
            }
        } catch (IOException |
                XmlPullParserException e
                )

        {
            e.printStackTrace();
        } finally

        {
            assert whereConnection != null;
            whereConnection.disconnect();
        }

        return resultSearch;
    }

    private static String buildSearchQuery(String city, String locale)
    {
        city = city.replaceAll(" ", "%20");

        return BASE_URL
                + "places.q(" + Uri.encode(city) + "%2A);count=" + COUNT_CITY
                + "?appid=" + APPID
                + "&lang=" + locale;
    }
}

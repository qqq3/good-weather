package org.asdtm.goodweather.utils;

import android.net.Uri;

import org.asdtm.goodweather.model.CitySearch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CityParser {

    private static final String TAG = "CityParser";
    private static final String ENDPOINT = "http://api.openweathermap.org/data/2.5/find";
    private static final String APPID = ApiKeys.OPEN_WEATHER_MAP_API_KEY;

    public static List<CitySearch> getCity(String query) {
        List<CitySearch> citySearchList = new ArrayList<>();
        CitySearch city;
        HttpURLConnection connection = null;

        try {
            URL url = getUrl(query);
            connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                byteArray.write(buffer, 0, bytesRead);
            }
            byteArray.close();

            JSONObject jsonObject = new JSONObject(byteArray.toString());
            JSONArray listArray = jsonObject.getJSONArray("list");

            int listArrayCount = listArray.length();

            for (int i = 0; i != listArrayCount; ++i) {
                city = new CitySearch();
                JSONObject resultObject = listArray.getJSONObject(i);
                city.setCityName(resultObject.getString("name"));

                JSONObject coordObject = resultObject.getJSONObject("coord");
                city.setLongitude(coordObject.getString("lon"));
                city.setLatitude(coordObject.getString("lat"));

                JSONObject sysObject = resultObject.getJSONObject("sys");
                city.setCountryCode(sysObject.getString("country"));
                citySearchList.add(city);
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return citySearchList;
    }

    private static URL getUrl(String query) throws MalformedURLException {
        String url = Uri.parse(ENDPOINT).buildUpon()
                        .appendQueryParameter("q", query)
                        .appendQueryParameter("type", "like")
                        .appendQueryParameter("cnt", "15")
                        .appendQueryParameter("APPID", APPID)
                        .build().toString();
        return new URL(url);
    }
}

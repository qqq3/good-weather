package org.asdtm.goodweather;

import android.net.Uri;

import org.asdtm.goodweather.utils.ApiKeys;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherRequest
{
    private static final String TAG = "WeatherRequest";
    private static final String ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";
    private static final String APPID = ApiKeys.OPEN_WEATHER_MAP_API_KEY;

    byte[] getWeatherByte(String location) throws IOException
    {
        // Создаем объект URL
        URL url = new URL(location);
        // Вызов метода openConnection() создает объект подключения к заданному url-адресу
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();

            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String url) throws IOException
    {
        return new String(getWeatherByte(url));
    }

    public String getItems(String lat, String lon, String units, String lang) throws IOException
    {

        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("lat", lat)
                .appendQueryParameter("lon", lon)
                .appendQueryParameter("APPID", APPID)
                .appendQueryParameter("units", units)
                .appendQueryParameter("lang", lang)
                .build()
                .toString();
        return getUrl(url);
    }
}

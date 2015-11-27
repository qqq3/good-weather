package org.asdtm.goodweather;


import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherRequest
{
    private static final String TAG = "WeatherRequest";
    private static final String ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";
    private String QUERY = "London";
    private static final String APPID = "7b1eaeea7795f54d52027369812383d0";

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

    public String getItems() throws IOException
    {
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("q", QUERY)
                .appendQueryParameter("APPID", APPID)
                .build()
                .toString();
        return getUrl(url);
    }
}

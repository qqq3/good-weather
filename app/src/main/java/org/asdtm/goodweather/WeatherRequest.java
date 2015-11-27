package org.asdtm.goodweather;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherRequest
{
    //http://api.openweathermap.org/data/2.5/weather?q=london&units=metric&APPID=7b1eaeea7795f54d52027369812383d0
    private final static String QUERY = "api.openweathermap.org/data/2.5/weather?q=";
    private final static String APPID = "7b1eaeea7795f54d52027369812383d0";

    byte[] getWeatherByte(String location) throws IOException
    {
        // Создаем объект URL
        URL url = new URL(location);
        // Вызов метода openConnection() создает объект подключения к заданному url-адресу
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = inputStream.read(buffer)) > 0)
            {
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
}

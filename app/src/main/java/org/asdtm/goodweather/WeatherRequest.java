package org.asdtm.goodweather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

public class WeatherRequest
{
    private static final String TAG = "WeatherRequest";

    byte[] getWeatherByte(URL url) throws IOException
    {
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

    public String getResultAsString(URL url) throws IOException
    {
        return new String(getWeatherByte(url));
    }

    public String getItems(String lat, String lon, String units, String lang) throws IOException
    {
        return getResultAsString(Utils.getWeatherForecastUrl(Constants.WEATHER_ENDPOINT, lat, lon, units, lang));
    }
}

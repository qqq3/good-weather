package org.asdtm.goodweather;


import java.io.BufferedReader;
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

    public String getWeather(String location) throws IOException
    {
        URL url = new URL(location);
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();

        try {

            StringBuffer buffer = new StringBuffer();
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            inputStream.close();

            return buffer.toString();
        } finally {
            connection.disconnect();
        }

    }

    public String getUrl(String url) throws IOException
    {
        return new String(getWeather(url));
    }
}

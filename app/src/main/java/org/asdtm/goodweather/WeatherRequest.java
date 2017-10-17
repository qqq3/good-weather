package org.asdtm.goodweather;

import android.os.Build;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

public class WeatherRequest
{
    private static final String TAG = "WeatherRequest";

    private class AsyncGetRequest extends Thread {
        static final String USER_AGENT = "User-Agent";
        static final String USER_AGENT_TEMPLATE = "UnifiedNlp/%s (Linux; Android %s)";
        private final AtomicBoolean done = new AtomicBoolean(false);
        private final URL url;
        private byte[] result;

        private AsyncGetRequest(URL url) {
            this.url = url;
        }

        @Override
        public void run() {
            synchronized (done) {
                try {
                    Log.d(TAG, "Requesting " + url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty(USER_AGENT, String.format(USER_AGENT_TEMPLATE, BuildConfig.VERSION_NAME, Build.VERSION.RELEASE));
                    connection.setDoInput(true);
                    InputStream inputStream = connection.getInputStream();
                    result = readStreamToEnd(inputStream);
                } catch (Exception e) {
                    Log.w(TAG, e);
                    }
                done.set(true);
                done.notifyAll();
            }
        }

        AsyncGetRequest asyncStart() {
            start();
            return this;
        }

        byte[] retrieveAllBytes() {
            if (!done.get()) {
                synchronized (done) {
                    while (!done.get()) {
                        try {
                            done.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
            return result;
        }

        String retrieveString() {
            byte[] result = retrieveAllBytes();
            if (result == null) {
                return "";
            }
            return new String(retrieveAllBytes());
        }

        private byte[] readStreamToEnd(InputStream is) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (is != null) {
                byte[] buff = new byte[1024];
                while (true) {
                    int nb = is.read(buff);
                    if (nb < 0) {
                        break;
                    }
                    bos.write(buff, 0, nb);
                }
                is.close();
            }
            return bos.toByteArray();
        }
    }
        
    String getWeatherByte(URL url) throws IOException
    {
        // Вызов метода openConnection() создает объект подключения к заданному url-адресу
        return new AsyncGetRequest(url).asyncStart().retrieveString();
        /*HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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
        }*/
    }

    public String getItems(String lat, String lon, String units, String lang) throws IOException
    {
        return getWeatherByte(Utils.getWeatherForecastUrl(Constants.WEATHER_ENDPOINT, lat, lon, units, lang));
    }
}

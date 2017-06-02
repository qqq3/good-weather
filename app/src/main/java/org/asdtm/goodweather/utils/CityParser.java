package org.asdtm.goodweather.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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
    private static final String ENDPOINT_SEARCH = "http://api.geonames.org/search";
    private static final String ENDPOINT_GEOCODER = "http://api.geonames.org/findNearbyJSON";

    public static List<CitySearch> getCoordinatesFromAddress(Context context, String query) {
        List<CitySearch> citiesList = new ArrayList<>();
        HttpURLConnection connection = null;
        String requestResult = "";
        try {
            String language = LanguageUtil.getLanguageName(PreferenceUtil.getLanguage(context));
            URL url = getSearchUrl(query, language);
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
            requestResult = byteArray.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return parseCity(requestResult, citiesList);
    }

    public static List<CitySearch> getAddressFromCoordinates(Context context, String latitude, String longitude) {
        List<CitySearch> citiesList = new ArrayList<>();
        HttpURLConnection connection = null;
        String requestResult = "";
        try {
            String language = LanguageUtil.getLanguageName(PreferenceUtil.getLanguage(context));
            URL url = getGeocoderUrl(latitude, longitude, language);
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
            requestResult = byteArray.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return parseCity(requestResult, citiesList);
    }

    private static List<CitySearch> parseCity(String data, List<CitySearch> citiesList) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray listArray = jsonObject.getJSONArray("geonames");
            int listArrayCount = listArray.length();
            for (int i = 0; i != listArrayCount; ++i) {
                CitySearch city = new CitySearch();
                JSONObject resultObject = listArray.getJSONObject(i);
                city.setCityName(resultObject.getString("name"));
                city.setToponymName(resultObject.getString("toponymName"));
                city.setAdminName(resultObject.getString("adminName1"));
                city.setCountryName(resultObject.getString("countryName"));
                city.setCountryCode(resultObject.getString("countryCode"));
                city.setLatitude(resultObject.getString("lat"));
                city.setLongitude(resultObject.getString("lng"));
                citiesList.add(city);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Cities: " + citiesList.toString());
        return citiesList;
    }

    private static URL getSearchUrl(String query, String language) throws MalformedURLException {
        String url = Uri.parse(ENDPOINT_SEARCH).buildUpon()
                .appendQueryParameter("q", query)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("maxRows", "15")
                .appendQueryParameter("username", "good_weather")
                .appendQueryParameter("featureClass", "P")
                .appendQueryParameter("type", "json")
                .appendQueryParameter("fuzzy", "0.8")
                .build().toString();
        return new URL(url);
    }

    private static URL getGeocoderUrl(String latitude, String longitude, String language) throws MalformedURLException {
        String url = Uri.parse(ENDPOINT_GEOCODER).buildUpon()
                .appendQueryParameter("username", "good_weather")
                .appendQueryParameter("lat", latitude)
                .appendQueryParameter("lng", longitude)
                .appendQueryParameter("lang", language)
                .build().toString();
        return new URL(url);
    }
}
